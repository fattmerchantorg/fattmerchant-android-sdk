package com.staxpayments.sdk

import com.staxpayments.exceptions.StaxException
import com.staxpayments.exceptions.StaxGeneralException
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.TransactionRequest
import com.staxpayments.api.models.CreditCard
import com.staxpayments.api.models.Invoice
import com.staxpayments.api.models.MobileReaderDetails
import com.staxpayments.api.models.PaymentMethod
import com.staxpayments.api.models.Transaction
import com.staxpayments.api.repository.CustomerRepository
import com.staxpayments.api.repository.InvoiceRepository
import com.staxpayments.api.repository.MobileReaderDriverRepository
import com.staxpayments.api.repository.PaymentMethodRepository
import com.staxpayments.api.repository.TransactionRepository
import com.staxpayments.api.StaxApi
import com.staxpayments.sdk.usecase.*
import com.staxpayments.sdk.usecase.CancelCurrentTransaction
import com.staxpayments.sdk.usecase.ConnectMobileReader
import com.staxpayments.sdk.usecase.DisconnectMobileReader
import com.staxpayments.sdk.usecase.GetConnectedMobileReader
import com.staxpayments.sdk.usecase.InitializeDrivers
import com.staxpayments.sdk.usecase.RefundMobileReaderTransaction
import com.staxpayments.sdk.usecase.SearchForReaders
import com.staxpayments.sdk.usecase.TakeMobileReaderPayment
import com.staxpayments.sdk.usecase.TakePayment
import com.staxpayments.sdk.usecase.TokenizePaymentMethod
import com.staxpayments.sdk.usecase.VoidMobileReaderTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class CommonStax internal constructor(internal var staxApi: StaxApi) {

    internal open var transactionRepository: TransactionRepository = object : TransactionRepository {
        override var staxApi: StaxApi = this@CommonStax.staxApi
    }

    internal open var invoiceRepository: InvoiceRepository = object : InvoiceRepository {
        override var staxApi: StaxApi = this@CommonStax.staxApi
    }

    internal open var customerRepository: CustomerRepository = object : CustomerRepository {
        override var staxApi: StaxApi = this@CommonStax.staxApi
    }

    internal open var paymentMethodRepository: PaymentMethodRepository = object : PaymentMethodRepository {
        override var staxApi: StaxApi = this@CommonStax.staxApi
    }

    internal open var initialized: Boolean = false

    /** True when Stax SDK is initialized. False otherwise */
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

    /**
     * Prepares the StaxService Client for taking payments
     *
     * This method will kick off procedures like preparing the mobile reader drivers so the client is ready to take
     * requests for payments
     */
    internal fun initialize(args: Map<String, Any>, onCompletion: () -> Unit, onError: (StaxException) -> Unit) {
        coroutineScope.launch {

            val merchant = staxApi.getSelf {
                onError(StaxException("Could not get reader settings", it.message))
            }?.merchant ?: run {
                onError(StaxException("Could not get reader settings", "Merchant object is null"))
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

            staxApi.getMobileReaderSettings {
                onError(StaxException("Could not get reader settings", it.message))
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
                ).start(onError)
            }

            InitializeDrivers(
                mobileReaderDriverRepository,
                mutatedArgs,
                coroutineContext
            ).start(onError)

            initialized = true

            onCompletion()
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
    fun getConnectedReader(onReaderFound: (MobileReader?) -> Unit, onFail: (StaxException) -> Unit) {
        if (!initialized) {
            onFail(StaxGeneralException.uninitialized)
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
            } catch (e: StaxException) {
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
     * @param onFail a block to run if this operation fails. Receives an [StaxException]
     */
    fun disconnectReader(mobileReader: MobileReader, onDisconnected: (Boolean) -> Unit, onFail: (StaxException) -> Unit) {
        if (!initialized) {
            onFail(StaxGeneralException.uninitialized)
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
     * @param onCompletion a block to run once the transaction is finished. Receives the completed
     * [Transaction]
     * @param onError a block to run if an error is thrown. Receives an [StaxException]
     */
    fun pay(transactionRequest: TransactionRequest, onCompletion: (Transaction) -> Unit, onError: (StaxException) -> Unit) {
        coroutineScope.launch {
            val takePaymentJob = TakePayment(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                request = transactionRequest,
                staxApi = staxApi,
                coroutineContext = coroutineContext
            )

            currentJob = takePaymentJob

            val result = takePaymentJob.start {
                onError(it)
                return@start
            }

            result?.let {
                onCompletion(it)
            }
        }
    }

    fun tokenize(bankAccount: com.staxpayments.api.models.BankAccount, onCompletion: (PaymentMethod) -> Unit, onError: (StaxException) -> Unit) {
        coroutineScope.launch {
            val tokenizeJob = TokenizePaymentMethod(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                bankAccount = bankAccount,
                coroutineContext = coroutineContext
            )

            currentJob = tokenizeJob
            val result = tokenizeJob.start {
                onError(it)
                return@start
            }

            result?.let {
                onCompletion(it)
            }
        }
    }

    fun tokenize(creditCard: CreditCard, onCompletion: (PaymentMethod) -> Unit, onError: (StaxException) -> Unit) {
        coroutineScope.launch {
            val tokenizeJob = TokenizePaymentMethod(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                creditCard = creditCard,
                coroutineContext = coroutineContext
            )

            currentJob = tokenizeJob
            val result = tokenizeJob.start {
                onError(it)
                return@start
            }

            result?.let {
                onCompletion(it)
            }
        }
    }

    /**
     * Captures a mobile reader transaction
     *
     * ## Signature
     * Stax should be assigned a [SignatureProviding] object by the time this transaction is
     * called. This object is responsible for providing a signature in case one is required to
     * complete the transaction. If the [SignatureProviding] object is not set on the receiver, then
     * the a blank signature will be submitted
     *
     * ```
     * Stax.shared()?.signatureProvider = object : SignatureProviding {
     *     override fun signatureRequired(onCompletion: (String) -> Unit) {
     *          var base64EncodedSignature = // ...
     *          onCompletion(base64EncodedSignature)
     *     }
     * }
     * ```
     *
     * ## Transaction Updates
     * Transaction updates will be delivered via the [TransactionUpdateListener]. This will need
     * to be set on the instance of Stax prior to running the transaction. This is optional and
     * omission of this step will not alter the flow of the transaction
     *
     * ```
     * Stax.shared()?.transactionUpdateListener = object: TransactionUpdateListener {
     *      override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
     *          print("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
     *      }
     * }
     * ```
     *
     *
     * @param request a [TransactionRequest] object that includes all the information needed to
     * run this transaction including [TransactionRequest.amount] and [TransactionRequest.tokenize]
     * @param onCompletion a block to run once the transaction is finished. Receives the completed
     * [Transaction]
     * @param onError a block to run if an error is thrown. Receives an [StaxException]
     */
    fun takeMobileReaderTransaction(
        request: TransactionRequest,
        onCompletion: (Transaction) -> Unit,
        onError: (StaxException) -> Unit
    ) {
        coroutineScope.launch {

            if (currentJob is TakeMobileReaderPayment && currentJob?.isActive == true) {
                onError(StaxException("Could not take mobile reader transaction", "Transaction in progress"))
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
                onError(it)
                return@start
            }

            result?.let {
                onCompletion(it)
            }
        }
    }

    /**
     * Captures a previously-authorized transaction
     *
     * @param transactionId The id of the transaction you want to capture
     * @param amount the amount that you want to capture. If null, then the original transaction amount will be captured
     * @param onCompletion Called when the operation is completed successfully. Receives a transaction
     * @param onError Receives any errors that happened while attempting the operation
     */
    fun capturePreAuthTransaction(
        transactionId: String,
        amount: Amount? = null,
        onCompletion: (Transaction) -> Unit,
        onError: (StaxException) -> Unit
    ) {
        coroutineScope.launch {
            CapturePreAuthTransaction(transactionId, staxApi, amount, coroutineContext).start {
                onError(it)
            }?.let { onCompletion(it) }
        }
    }

    /**
     * Voids the given transaction and returns a new [Transaction] that represents the void in Stax
     *
     * @param transactionId The id of the transaction to void
     * @param onCompletion A block to execute with the new, voided [Transaction]
     * @param onError a block to run in case an error occurs
     */
    fun voidTransaction(
        transactionId: String,
        onCompletion: (Transaction) -> Unit,
        onError: (StaxException) -> Unit
    ) {
        coroutineScope.launch {
            VoidTransaction(
                transactionId,
                staxApi,
                coroutineContext
            ).start {
                onError(it)
            }?.let { onCompletion(it) }
        }
    }

    /**
     * Voids the given transaction and returns a new [Transaction] that represents the void in Stax
     *
     * @param transaction The transaction to void
     * @param onCompletion A block to execute with the new, voided [Transaction]
     * @param onError a block to run in case an error occurs
     */
    fun voidMobileReaderTransaction(
        transaction: Transaction,
        onCompletion: (Transaction) -> Unit,
        onError: (StaxException) -> Unit
    ) {
        coroutineScope.launch {
            VoidMobileReaderTransaction(
                mobileReaderDriverRepository,
                transactionRepository,
                transaction,
                coroutineContext
            ).start {
                onError(it)
            }?.let { onCompletion(it) }
        }
    }

    /**
     * Refunds the given transaction and returns a new [Transaction] that represents the refund in Stax
     *
     * @param transaction
     * @param onCompletion
     * @param onError a block to run in case an error occurs
     */
    fun refundMobileReaderTransaction(
        transaction: Transaction,
        onCompletion: (Transaction) -> Unit,
        onError: (error: StaxException) -> Unit
    ) = refundMobileReaderTransaction(transaction, null, onCompletion, onError)

    /**
     * Refunds the given transaction and returns a new [Transaction] that represents the refund in Stax
     *
     * @param transaction
     * @param refundAmount The [Amount] to refund. When present, this **must** be greater than zero and lesser than or equal to the transaction total
     * @param onCompletion
     * @param onError a block to run in case an error occurs
     */
    fun refundMobileReaderTransaction(
        transaction: Transaction,
        refundAmount: Amount? = null,
        onCompletion: (Transaction) -> Unit,
        onError: (error: StaxException) -> Unit
    ) {
        coroutineScope.launch {
            RefundMobileReaderTransaction(
                mobileReaderDriverRepository,
                transactionRepository,
                transaction,
                refundAmount,
                staxApi
            ).start {
                onError(it)
            }?.let { onCompletion(it) }
        }
    }

    /**
     * Attempts to cancel a current mobile reader [transaction]
     *
     * @param onCompletion
     * @param onError a block to run in case an error occurs
     */
    fun cancelMobileReaderTransaction(
        onCompletion: (Boolean) -> Unit,
        onError: (error: StaxException) -> Unit
    ) {
        coroutineScope.launch {
            onCompletion(
                CancelCurrentTransaction(
                    coroutineContext,
                    mobileReaderDriverRepository
                ).start(onError)
            )
        }
    }

    /**
     * Gets the invoices
     *
     * @param onCompletion a block of code to execute when finished
     */
    fun getInvoices(onCompletion: (List<Invoice>) -> Unit, onError: (StaxException) -> Unit) {
        coroutineScope.launch {
            invoiceRepository.get(onError)
                ?.let { onCompletion(it) }
        }
    }

    /**
     * Gets the transactions
     *
     * @param onCompletion a block of code to execute when finished
     */
    fun getTransactions(onCompletion: (List<Transaction>) -> Unit, onError: (StaxException) -> Unit) {
        coroutineScope.launch {
            transactionRepository.get(onError)
                ?.let { onCompletion(it) }
        }
    }
}
