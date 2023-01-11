package com.fattmerchant.omni.usecase

import com.fattmerchant.android.dejavoo.DejavooDriver
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.*
import com.fattmerchant.omni.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class TakePaymentTerminalPayment(
    val invoiceRepository: InvoiceRepository,
    val customerRepository: CustomerRepository,
    val paymentMethodRepository: PaymentMethodRepository,
    val transactionRepository: TransactionRepository,
    val request: TransactionRequest,
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    class TakeMobileReaderPaymentException(message: String? = null) :
        OmniException("Could not take mobile reader payment", message)

    suspend fun start(onError: (OmniException) -> Unit): Transaction? = coroutineScope {

        // Get the driver responsible for taking the payment
        var terminalDriver: PaymentTerminalDriver = mobileReaderDriverRepository.getTerminal()
            ?: run {
            onError(TakeMobileReaderPaymentException("Terminal not initialized"))
                return@coroutineScope null
            }

        var invoice = Invoice()

        // Set the transactionId of the request
        if (request.transactionId == null) {
            request.transactionId = UUID.randomUUID().toString()
        }

        // Check if we are passing an invoice id
        request.invoiceId?.let {
            if (it.isBlank()) {
                onError(TakeMobileReaderPaymentException("Could not create invoice."))
                return@coroutineScope  null
            }
            // Check if the invoice exists
            invoice = invoiceRepository.getById(it) {
                onError(TakeMobileReaderPaymentException("Invoice with given id not found"))
            } ?: return@coroutineScope null
        } ?: run {
            // If not create the invoice
            invoice = invoiceRepository.create(
                Invoice().apply {
                    total = request.amount.dollarsString()
                    url = "https://fattpay.com/#/bill/"
                    meta = mapOf("subtotal" to request.amount.dollarsString())
                }
            ) {
                onError(it)
            } ?: return@coroutineScope null
        }

        // Take the payment on the terminal
        val result: TransactionResult

        try {
            result = terminalDriver.performTransaction(request)
        } catch (e: MobileReaderDriver.PerformTransactionException) {
            onError(TakeMobileReaderPaymentException(e.detail))
            return@coroutineScope null
        } catch (e: Error) {
            // Something went wrong while performing the transaction
            onError(TakeMobileReaderPaymentException(e.message))
            return@coroutineScope null
        }

        // Check if transaction cancelled
        if (result.message?.contains("busy") == true && result.authCode == null) {
            onError(TakeMobileReaderPaymentException("Service Busy"))
            return@coroutineScope null
        }

        if (result.message == "Canceled") {
            onError(TakeMobileReaderPaymentException("User Cancelled"))
            return@coroutineScope null
        }

        val voidAndFail = { exception: OmniException ->
            // TODO
//            reader.voidTransaction(result) {
//                onError(exception)
            onError(exception)
//            }
        }

        val customer = when {
            invoice.customerId != null -> {
                // Check if the invoice exists
                customerRepository.getById(invoice.customerId!!) {
                    voidAndFail(TakeMobileReaderPaymentException("Customer with given id not found"))
                } ?: return@coroutineScope null
            }

            request.customerId != null -> {
                customerRepository.getById(request.customerId!!) {
                    voidAndFail(TakeMobileReaderPaymentException("Customer with given id not found"))
                } ?: return@coroutineScope null
            }

            else -> {
                customerRepository.create(
                    Customer().apply {
                        firstname = if(result.transactionSource.equals("contactless", true)) "Mobile Device" else result.cardHolderFirstName ?: "SWIPE"
                        lastname = if(result.transactionSource.equals("contactless", true)) "Customer" else result.cardHolderLastName ?: "CUSTOMER"
                    }
                ) {
                    voidAndFail(it)
                } ?: return@coroutineScope null
            }

        }

        // Create a PaymentMethod
        val paymentMethod = paymentMethodRepository.create(
            result.generatePaymentMethod().apply {
                merchantId = customer.merchantId
                customerId = customer.id
            }
        ) {
            voidAndFail(it)
        } ?: return@coroutineScope null

        // Associate payment method and invoice with customer
        invoice.paymentMethodId = paymentMethod.id
        invoice.customerId = customer.id

        // If the invoice already has meta, then we don't want to replace it but rather merge in
        // the new fields we have
        invoice.meta = invoice.meta?.let {
            val newMeta = it.toMutableMap()
            newMeta.putAll(result.invoiceMeta())
            newMeta
        } ?: result.invoiceMeta()

        // Update invoice
        invoiceRepository.update(invoice) {
            voidAndFail(it)
        } ?: return@coroutineScope null

        // Create transaction
        val createdTransaction = transactionRepository.create(
            result.generateTransaction().apply {
                id = request.transactionId
                paymentMethodId = paymentMethod.id
                customerId = customer.id
                invoiceId = invoice.id
            }
        ) {
            voidAndFail(it)
        } ?: return@coroutineScope null

        return@coroutineScope createdTransaction
    }

    /**
     * Finds a mobile reader driver that is ready to take a payment
     *
     * @param repo A [MobileReaderDriverRepository] to look for the readers in
     * @return a [MobileReaderDriver], if found
     */
    private suspend fun getAvailableMobileReaderDriver(repo: MobileReaderDriverRepository): MobileReaderDriver? = repo
        .getInitializedDrivers()
        .firstOrNull { it.isReadyToTakePayment() }

}