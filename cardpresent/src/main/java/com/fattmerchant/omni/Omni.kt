package com.fattmerchant.omni

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.*
import com.fattmerchant.omni.networking.OmniApi
import com.fattmerchant.omni.usecase.*
import kotlinx.coroutines.*

open class Omni(var omniApi: OmniApi) {

    open var transactionRepository: TransactionRepository = object : TransactionRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    open var invoiceRepository: InvoiceRepository = object : InvoiceRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    open var customerRepository: CustomerRepository = object : CustomerRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    open var paymentMethodRepository: PaymentMethodRepository = object : PaymentMethodRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    open lateinit var mobileReaderDriverRepository: MobileReaderDriverRepository
    var coroutineScope = MainScope()
    var currentJob: CoroutineScope? = null

    /**
     * Prepares the OmniService Client for taking payments
     *
     * This method will kick off procedures like preparing the mobile reader drivers so the client is ready to take
     * requests for payments
     */
    fun initialize(args: Map<String, Any>, completion: () -> Unit, error: (OmniException) -> Unit) {
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
                val connected = ConnectMobileReader(
                        coroutineContext,
                        mobileReaderDriverRepository,
                        mobileReader
                ).start()

                if (connected) {
                    onConnected(mobileReader)
                } else {
                    onFail("Could not connect to mobile reader")
                }

            } catch (e: OmniException) {
                onFail(e.detail ?: e.message ?: "Could not connect mobile reader")
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
                    coroutineContext = coroutineContext
            )

            currentJob = takePaymentJob

            val result = takePaymentJob.start {
                error(OmniException("Could not take mobile reader transaction", it.message))
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
    ) {
        coroutineScope.launch {
            RefundMobileReaderTransaction(
                    mobileReaderDriverRepository,
                    transactionRepository,
                    transaction
            ).start {
                error(it)
            }?.let { completion(it) }
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
