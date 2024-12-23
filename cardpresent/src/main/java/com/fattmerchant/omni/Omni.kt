package com.fattmerchant.omni

import android.content.Context
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.BankAccount
import com.fattmerchant.omni.data.models.CreditCard
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.MobileReaderDetails
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.PaymentMethod
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.CustomerRepository
import com.fattmerchant.omni.data.repository.InvoiceRepository
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import com.fattmerchant.omni.data.repository.PaymentMethodRepository
import com.fattmerchant.omni.data.repository.TransactionRepository
import com.fattmerchant.omni.networking.OmniApi
import com.fattmerchant.omni.usecase.*
import com.fattmerchant.omni.usecase.CancelCurrentTransaction
import com.fattmerchant.omni.usecase.ConnectMobileReader
import com.fattmerchant.omni.usecase.DisconnectMobileReader
import com.fattmerchant.omni.usecase.GetConnectedMobileReader
import com.fattmerchant.omni.usecase.InitializeDrivers
import com.fattmerchant.omni.usecase.RefundMobileReaderTransaction
import com.fattmerchant.omni.usecase.SearchForReaders
import com.fattmerchant.omni.usecase.TakeMobileReaderPayment
import com.fattmerchant.omni.usecase.TakePayment
import com.fattmerchant.omni.usecase.TokenizePaymentMethod
import com.fattmerchant.omni.usecase.VoidMobileReaderTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OmniGeneralException(detail: String) : OmniException("Omni General Error", detail) {
    companion object {
        val unknown = OmniGeneralException("Unknown error has occurred")
        val uninitialized = OmniGeneralException("Omni has not been initialized yet")
    }
}

open class Omni internal constructor(internal var omniApi: OmniApi) {

    internal open var transactionRepository: TransactionRepository = object : TransactionRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    internal open var invoiceRepository: InvoiceRepository = object : InvoiceRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    internal open var customerRepository: CustomerRepository = object : CustomerRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    internal open var paymentMethodRepository: PaymentMethodRepository = object : PaymentMethodRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    internal open var initialized: Boolean = false

    /** True when Omni is initialized. False otherwise */
    public val isInitialized get() = initialized

    /** Responsible for providing signatures for transactions, when required */
    open var signatureProvider: SignatureProviding? = null

    /** Receives notifications about transaction events such as when a card is swiped */
    open var transactionUpdateListener: TransactionUpdateListener? = null

    /** Receives user notifications that are used for prompts */
    open var userNotificationListener: UserNotificationListener? = null

    /** Receives notifications about reader connection events */
    open var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    internal open lateinit var mobileReaderDriverRepository: MobileReaderDriverRepository
    internal var coroutineScope = MainScope()
    private var currentJob: CoroutineScope? = null

    /** Receives system notifications for USB devices being attached */
    internal var usbAccessoryListener: UsbAccessoryListener? = null
    internal var accessoryHelper: AccessoryHelper? = null

    /**
     * Prepares the OmniService Client for taking payments
     *
     * This method will kick off procedures like preparing the mobile reader drivers so the client is ready to take
     * requests for payments
     */
    internal fun initialize(args: Map<String, Any>, completion: () -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {

            val merchant = omniApi.getSelf {
                error(OmniException("Could not get reader settings", it.message))
            }?.merchant ?: run {
                error(OmniException("Could not get reader settings", "Merchant object is null"))
                return@launch
            }

            val mutatedArgs = args.toMutableMap()

            // AWC
            val awcDetails = MobileReaderDetails.AWCDetails()
            merchant.emvTerminalId()?.let { awcDetails.terminalId = it }
            merchant.emvTerminalSecret()?.let { awcDetails.terminalSecret = it }
            mutatedArgs["awc"] = awcDetails

            // NMI
            val nmiDetails = MobileReaderDetails.NMIDetails()
            merchant.emvPassword()?.let { nmiDetails.securityKey = it }
            mutatedArgs["nmi"] = nmiDetails

            // Setup USB Listener
            usbAccessoryListener?.let { listener ->
                val appContext = args["appContext"] as? Context
                appContext?.let { context ->
                    accessoryHelper = AccessoryHelper(context, listener)
                }
            }

            omniApi.getMobileReaderSettings {
                // error(OmniException("Could not get reader settings", it.message))
            }?.let { mobileReaderDetails ->
                mobileReaderDetails.nmi?.let {
                    mutatedArgs["nmi"] = it
                }

                mobileReaderDetails.anywhereCommerce?.let {
                    mutatedArgs["awc"] = it
                }

                InitializeDrivers(
                    mobileReaderDriverRepository,
                    mutatedArgs,
                    coroutineContext
                ).start(error)
            }

            InitializeDrivers(
                mobileReaderDriverRepository,
                mutatedArgs,
                coroutineContext
            ).start(error)

            initialized = true

            completion()
        }
    }

    /**
     * Searches for all readers that are available given across all the mobile reader drivers
     */
    fun getAvailableReaders(onReadersFound: (List<MobileReader>) -> Unit) {
        coroutineScope.launch {
            val searchJob = SearchForReaders(
                mobileReaderDriverRepository,
                mapOf(),
                coroutineContext
            )
            onReadersFound(searchJob.start())
        }
    }

    /**
     * Returns the connected mobile reader
     *
     * @param onReaderFound a block to run once finished. Receives the connected mobile reader, if
     * any
     * @param onFail a block to run if an error is thrown. Receives the error
     */
    fun getConnectedReader(onReaderFound: (MobileReader?) -> Unit, onFail: (OmniException) -> Unit) {
        if (!initialized) {
            onFail(OmniGeneralException.uninitialized)
            return
        }

        coroutineScope.launch {
            val job = GetConnectedMobileReader(
                coroutineContext,
                mobileReaderDriverRepository
            )

            onReaderFound(job.start(onFail))
        }
    }

    /**
     * Attempts to connect to the given [MobileReader]
     */
    fun connectReader(mobileReader: MobileReader, onConnected: (MobileReader) -> Unit, onFail: (String) -> Unit) {
        coroutineScope.launch {
            try {
                val connectedReader = ConnectMobileReader(
                    coroutineContext,
                    mobileReaderDriverRepository,
                    mobileReader,
                    mobileReaderConnectionStatusListener
                ).start()

                if (connectedReader != null) {
                    onConnected(connectedReader)
                } else {
                    onFail("Could not connect to mobile reader")
                }
            } catch (e: OmniException) {
                onFail(e.detail ?: e.message ?: "Could not connect mobile reader")
            }
        }
    }

    /**
     * Attempts to connect to the given [MobileReader]
     *
     * returns a [MobileReader]
     */
    suspend fun connectReader(mobileReader: MobileReader): MobileReader? = withContext(Dispatchers.Main) {
        return@withContext ConnectMobileReader(
            coroutineContext,
            mobileReaderDriverRepository,
            mobileReader,
            mobileReaderConnectionStatusListener
        ).start()
    }

    /**
     * Attempts to disconnect the given [MobileReader]
     *
     * @param mobileReader the [MobileReader] to disconnect
     * @param onDisconnected a block to run once finished. It will receive true if the reader was disconencted
     * @param onFail a block to run if this operation fails. Receives an [OmniException]
     */
    fun disconnectReader(mobileReader: MobileReader?, onDisconnected: (Boolean) -> Unit, onFail: (OmniException) -> Unit) {
        if (!initialized) {
            onFail(OmniGeneralException.uninitialized)
            return
        }

        coroutineScope.launch {
            val job = DisconnectMobileReader(
                coroutineContext,
                mobileReaderDriverRepository,
                mobileReader
            )
            onDisconnected(job.start(onFail))
        }
    }

    /**
     * Charges a transaction
     *
     * @param transactionRequest a [TransactionRequest] object that includes all the information needed to
     * run this transaction including [TransactionRequest.amount] and [TransactionRequest.tokenize]
     * @param completion a block to run once the transaction is finished. Receives the completed
     * [Transaction]
     * @param error a block to run if an error is thrown. Receives an [OmniException]
     */
    fun pay(transactionRequest: TransactionRequest, completion: (Transaction) -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {
            val takePaymentJob = TakePayment(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                request = transactionRequest,
                omniApi = omniApi,
                coroutineContext = coroutineContext
            )

            currentJob = takePaymentJob

            val result = takePaymentJob.start {
                error(it)
                return@start
            }

            result?.let {
                completion(it)
            }
        }
    }

    fun tokenize(bankAccount: BankAccount, completion: (PaymentMethod) -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {
            val tokenizeJob = TokenizePaymentMethod(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                bankAccount = bankAccount,
                coroutineContext = coroutineContext
            )

            currentJob = tokenizeJob
            val result = tokenizeJob.start {
                error(it)
                return@start
            }

            result?.let {
                completion(it)
            }
        }
    }

    fun tokenize(creditCard: CreditCard, completion: (PaymentMethod) -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {
            val tokenizeJob = TokenizePaymentMethod(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                creditCard = creditCard,
                coroutineContext = coroutineContext
            )

            currentJob = tokenizeJob
            val result = tokenizeJob.start {
                error(it)
                return@start
            }

            result?.let {
                completion(it)
            }
        }
    }

    /**
     * Captures a mobile reader transaction
     *
     * ## Signature
     * Omni should be assigned a [SignatureProviding] object by the time this transaction is
     * called. This object is responsible for providing a signature in case one is required to
     * complete the transaction. If the [SignatureProviding] object is not set on the receiver, then
     * the a blank signature will be submitted
     *
     * ```
     * Omni.shared()?.signatureProvider = object : SignatureProviding {
     *     override fun signatureRequired(completion: (String) -> Unit) {
     *          var base64EncodedSignature = // ...
     *          completion(base64EncodedSignature)
     *     }
     * }
     * ```
     *
     * ## Transaction Updates
     * Transaction updates will be delivered via the [TransactionUpdateListener]. This will need
     * to be set on the instance of Omni prior to running the transaction. This is optional and
     * omission of this step will not alter the flow of the transaction
     *
     * ```
     * Omni.shared()?.transactionUpdateListener = object: TransactionUpdateListener {
     *      override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
     *          print("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
     *      }
     * }
     * ```
     *
     *
     * @param request a [TransactionRequest] object that includes all the information needed to
     * run this transaction including [TransactionRequest.amount] and [TransactionRequest.tokenize]
     * @param completion a block to run once the transaction is finished. Receives the completed
     * [Transaction]
     * @param error a block to run if an error is thrown. Receives an [OmniException]
     */
    fun takeMobileReaderTransaction(
        request: TransactionRequest,
        completion: (Transaction) -> Unit,
        error: (OmniException) -> Unit
    ) {
        coroutineScope.launch {

            if (currentJob is TakeMobileReaderPayment && currentJob?.isActive == true) {
                error(OmniException("Could not take mobile reader transaction", "Transaction in progress"))
                return@launch
            }

            val takePaymentJob = TakeMobileReaderPayment(
                mobileReaderDriverRepository = mobileReaderDriverRepository,
                invoiceRepository = invoiceRepository,
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                transactionRepository = transactionRepository,
                request = request,
                signatureProvider = signatureProvider,
                transactionUpdateListener = transactionUpdateListener,
                userNotificationListener = userNotificationListener,
                coroutineContext = coroutineContext
            )

            currentJob = takePaymentJob

            val result = takePaymentJob.start {
                error(it)
                return@start
            }

            result?.let {
                completion(it)
            }
        }
    }

    /**
     * Captures a previously-authorized transaction
     *
     * @param transactionId The id of the transaction you want to capture
     * @param amount the amount that you want to capture. If null, then the original transaction amount will be captured
     * @param completion Called when the operation is completed successfully. Receives a transaction
     * @param error Receives any errors that happened whiel attempting the operation
     */
    fun capturePreauthTransaction(
        transactionId: String,
        amount: Amount? = null,
        completion: (Transaction) -> Unit,
        error: (OmniException) -> Unit
    ) {
        coroutineScope.launch {
            CapturePreauthTransaction(transactionId, omniApi, amount, coroutineContext).start {
                error(it)
            }?.let { completion(it) }
        }
    }

    /**
     * Voids the given transaction and returns a new [Transaction] that represents the void in Omni
     *
     * @param transaction The transaction to void
     * @param completion A block to execute with the new, voided [Transaction]
     * @param error a block to run in case an error occurs
     */
    fun voidTransaction(
        transactionId: String,
        completion: (Transaction) -> Unit,
        error: (OmniException) -> Unit
    ) {
        coroutineScope.launch {
            VoidTransaction(
                transactionId,
                omniApi,
                coroutineContext
            ).start {
                error(it)
            }?.let { completion(it) }
        }
    }

    /**
     * Voids the given transaction and returns a new [Transaction] that represents the void in Omni
     *
     * @param transaction The transaction to void
     * @param completion A block to execute with the new, voided [Transaction]
     * @param error a block to run in case an error occurs
     */
    fun voidMobileReaderTransaction(
        transaction: Transaction,
        completion: (Transaction) -> Unit,
        error: (OmniException) -> Unit
    ) {
        coroutineScope.launch {
            VoidMobileReaderTransaction(
                mobileReaderDriverRepository,
                transactionRepository,
                transaction,
                coroutineContext
            ).start {
                error(it)
            }?.let { completion(it) }
        }
    }

    /**
     * Refunds the given transaction and returns a new [Transaction] that represents the refund in Omni
     *
     * @param transaction
     * @param completion
     * @param error a block to run in case an error occurs
     */
    fun refundMobileReaderTransaction(
        transaction: Transaction,
        completion: (Transaction) -> Unit,
        error: (error: OmniException) -> Unit
    ) = refundMobileReaderTransaction(transaction, null, completion, error)

    /**
     * Refunds the given transaction and returns a new [Transaction] that represents the refund in Omni
     *
     * @param transaction
     * @param refundAmount The [Amount] to refund. When present, this **must** be greater than zero and lesser than or equal to the transaction total
     * @param completion
     * @param error a block to run in case an error occurs
     */
    fun refundMobileReaderTransaction(
        transaction: Transaction,
        refundAmount: Amount? = null,
        completion: (Transaction) -> Unit,
        error: (error: OmniException) -> Unit
    ) {
        coroutineScope.launch {
            RefundMobileReaderTransaction(
                mobileReaderDriverRepository,
                transactionRepository,
                transaction,
                refundAmount,
                omniApi
            ).start {
                error(it)
            }?.let { completion(it) }
        }
    }

    /**
     * Attempts to cancel a current mobile reader [transaction]
     *
     * @param completion
     * @param error a block to run in case an error occurs
     */
    fun cancelMobileReaderTransaction(
        completion: (Boolean) -> Unit,
        error: (error: OmniException) -> Unit
    ) {
        coroutineScope.launch {
            completion(
                CancelCurrentTransaction(
                    coroutineContext,
                    mobileReaderDriverRepository
                ).start(error)
            )
        }
    }

    /**
     * Gets the invoices
     *
     * @param completion a block of code to execute when finished
     */
    fun getInvoices(completion: (List<Invoice>) -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {
            invoiceRepository.get(error)
                ?.let { completion(it) }
        }
    }

    /**
     * Gets the transactions
     *
     * @param completion a block of code to execute when finished
     */
    fun getTransactions(completion: (List<Transaction>) -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {
            transactionRepository.get(error)
                ?.let { completion(it) }
        }
    }
}
