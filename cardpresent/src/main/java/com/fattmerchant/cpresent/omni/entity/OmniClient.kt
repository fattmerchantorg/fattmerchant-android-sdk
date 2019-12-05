package com.fattmerchant.cpresent.omni.entity

import com.fattmerchant.cpresent.omni.entity.models.Invoice
import com.fattmerchant.cpresent.omni.entity.models.OmniException
import com.fattmerchant.cpresent.omni.entity.models.Transaction
import com.fattmerchant.cpresent.omni.entity.repository.*
import com.fattmerchant.cpresent.omni.networking.OmniApi
import com.fattmerchant.cpresent.omni.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

interface OmniClient : CoroutineScope {

    val mobileReaderDriverRepository: MobileReaderDriverRepository
    val transactionRepository: TransactionRepository
    val invoiceRepository: InvoiceRepository
    val customerRepository: CustomerRepository
    val paymentMethodRepository: PaymentMethodRepository

    var omniApi: OmniApi

    /**
     * Prepares the OmniService Client for taking payments
     *
     * This method will kick off procedures like preparing the mobile reader drivers so the client is ready to take
     * requests for payments
     */
    fun initialize(args: Map<String, Any>, error: (Error) -> Unit) = launch {

        // Verify that the apiKey corresponds to a real merchant
        val merchant = omniApi.getMerchant {
            error(Error("Could not find merchant for given apiKey"))
        } ?: return@launch

        // Get the nmiApiKey from the merchant
        val argsWithMerchant = args.toMutableMap().apply {
            set("merchant", merchant)
        }

        try {
            InitializeDrivers(
                mobileReaderDriverRepository,
                argsWithMerchant,
                coroutineContext
            ).start()
        } catch (e: Error) {
            error(e)
        }
    }

    /**
     * Searches for all readers that are available given across all the mobile reader drivers
     */
    fun getAvailableReaders(onReadersFound: (List<MobileReader>) -> Unit) = launch {
        val searchJob = SearchForReaders(
            mobileReaderDriverRepository,
            mapOf(),
            coroutineContext
        )
        onReadersFound(searchJob.start())
    }

    /**
     * Attempts to connect to the given [MobileReader]
     */
    fun connectReader(mobileReader: MobileReader, onConnected: (MobileReader) -> Unit, onFail: (String) -> Unit) =
        launch {
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

    /**
     * Captures a mobile reader transaction
     */
    fun takeMobileReaderTransaction(
        request: TransactionRequest,
        completion: (Transaction) -> Unit,
        error: (Error) -> Unit
    ) {
        launch {
            val result = TakeMobileReaderPayment(
                mobileReaderDriverRepository = mobileReaderDriverRepository,
                invoiceRepository = invoiceRepository,
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                transactionRepository = transactionRepository,
                request = request,
                coroutineContext = coroutineContext
            ).start {
                error(it)
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
        error: (Error) -> Unit
    ) {
        launch {
            VoidMobileReaderTransaction(
                mobileReaderDriverRepository,
                transactionRepository,
                transaction,
                coroutineContext
            ).start(error)
                ?.let { completion(it) }
        }
    }

    /**
     * TODO
     *
     * @param transaction
     * @param completion
     * @param error a block to run in case an error occurs
     */
    fun refundMobileReaderTransaction(
        transaction: Transaction,
        completion: (Transaction) -> Unit,
        error: (error: Error) -> Unit
    ) {
        launch {
            VoidMobileReaderTransaction(
                mobileReaderDriverRepository,
                transactionRepository,
                transaction,
                coroutineContext
            ).start(error)
                ?.let { completion(it) }
        }
    }

    /**
     *
     *
     * @param transaction
     * @param completion
     */
    fun refundOrVoidMobileReaderTransaction(transaction: Transaction, completion: (Transaction) -> Unit) {

    }

    /**
     * Gets the invoices
     *
     * @param completion a block of code to execute when finished
     */
    fun getInvoices(completion: (List<Invoice>) -> Unit, error: (Error) -> Unit) {
        launch {
            invoiceRepository.get(error)
                ?.let { completion(it) }
        }
    }

    /**
     * Gets the transactions
     *
     * @param completion a block of code to execute when finished
     */
    fun getTransactions(completion: (List<Transaction>) -> Unit, error: (Error) -> Unit) {
        launch {
            transactionRepository.get(error)
                ?.let { completion(it) }
        }
    }

}
