package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.*
import com.fattmerchant.omni.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

internal class TakeMobileReaderPayment(
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    val invoiceRepository: InvoiceRepository,
    val customerRepository: CustomerRepository,
    val paymentMethodRepository: PaymentMethodRepository,
    val transactionRepository: TransactionRepository,
    val request: TransactionRequest,
    val signatureProvider: SignatureProviding? = null,
    val transactionUpdateListener: TransactionUpdateListener? = null,
    val userNotificationListener: UserNotificationListener? = null,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    class TakeMobileReaderPaymentException(message: String? = null) :
        OmniException("Could not take mobile reader payment", message)

    suspend fun start(onError: (OmniException) -> Unit): Transaction? = coroutineScope {

        // Get the reader responsible for taking the payment
        val reader = getAvailableMobileReaderDriver(mobileReaderDriverRepository)
        if (reader == null) {
            onError(TakeMobileReaderPaymentException("No available mobile reader"))
            return@coroutineScope null
        }

        var invoice = Invoice()
        var customer: Customer? = null

        // Try to get the customer by id. If we couldn't get it, throw an error
        request.customerId?.let { customerId ->
            val retrievedCustomer = customerRepository.getById(customerId) { }

            if (retrievedCustomer != null) {
                customer = retrievedCustomer
            } else {
                onError(TakeMobileReaderPaymentException("Could not retrieve customer with id $customerId"))
                return@coroutineScope null
            }
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

        // Take the mobile reader payment
        val result: TransactionResult

        try {
            result = reader.performTransaction(request, signatureProvider, transactionUpdateListener, userNotificationListener)
        } catch (e: MobileReaderDriver.PerformTransactionException) {
            onError(TakeMobileReaderPaymentException(e.detail))
            return@coroutineScope null
        } catch (e: Error) {
            // Something went wrong while performing the transaction
            onError(TakeMobileReaderPaymentException(e.message))
            return@coroutineScope null
        }

        // Create customer
        if (customer == null) {
            customer = customerRepository.create(
                result.generateCustomer()
            ) {
                onError(it)
            } ?: return@coroutineScope null
        }

        // Create a PaymentMethod
        val paymentMethod = paymentMethodRepository.create(
            result.generatePaymentMethod().apply {
                merchantId = customer?.merchantId
                customerId = customer?.id
            }
        ) {
            onError(it)
        } ?: return@coroutineScope null

        // Associate payment method and invoice with customer
        invoice.paymentMethodId = paymentMethod.id
        invoice.customerId = customer?.id

        // Update invoice
        invoiceRepository.update(invoice) {
            onError(it)
        } ?: return@coroutineScope null

        // Create transaction
        transactionRepository.create(
            result.generateTransaction().apply {
                paymentMethodId = paymentMethod.id
                customerId = customer?.id
                invoiceId = invoice.id
            }
        ) {
            onError(it)
        }
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