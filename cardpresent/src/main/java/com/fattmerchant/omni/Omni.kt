package com.fattmerchant.omni

import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.*
import com.fattmerchant.omni.networking.OmniApi
import com.fattmerchant.omni.usecase.*
import kotlinx.coroutines.*

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

    /** Responsible for providing signatures for transactions, when required */
    open var signatureProvider: SignatureProviding? = null

    /** Receives notifications about transaction events such as when a card is swiped */
    open var transactionUpdateListener: TransactionUpdateListener? = null

    /** Receives notifications about reader connection events */
    open var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    internal open lateinit var mobileReaderDriverRepository: MobileReaderDriverRepository
    internal var coroutineScope = MainScope()
    private var currentJob: CoroutineScope? = null

    /**
     * Prepares the OmniService Client for taking payments
     *
     * This method will kick off procedures like preparing the mobile reader drivers so the client is ready to take
     * requests for payments
     */
    internal fun initialize(args: Map<String, Any>, completion: () -> Unit, error: (OmniException) -> Unit) {
        coroutineScope.launch {
            // Verify that the apiKey corresponds to a real merchant
            val merchant = omniApi.getMerchant {
                error(OmniException("Could not initialize Omni", it.message))
            } ?: return@launch

            // Get the nmiApiKey from the merchant
            val argsWithMerchant = args.toMutableMap().apply {
                set("merchant", merchant)
            }

            InitializeDrivers(
                    mobileReaderDriverRepository,
                    argsWithMerchant,
                    coroutineContext
            ).start(error)

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

    fun disconnectReader(mobileReader: MobileReader, onDisconnected: (Boolean) -> Unit, onFail: (String) -> Unit) {
        coroutineScope.launch {
            try {
                val disconnected = DisconnectMobileReader(
                        coroutineContext,
                        mobileReaderDriverRepository,
                        mobileReader
                ).start()

                if (disconnected) {
                    onDisconnected(true)
                } else {
                    onFail("Could not disconnect mobile reader")
                }
            } catch (e: OmniException) {
                onFail(e.detail ?: e.message ?: "Could not disconnect mobile reader")
            }
        }
    }

    /**
     * Captures a mobile reader transaction
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
            completion(CancelCurrentTransaction(
                    coroutineContext,
                    mobileReaderDriverRepository
            ).start(error))
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
