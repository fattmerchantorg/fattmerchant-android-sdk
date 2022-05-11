package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.*
import com.fattmerchant.omni.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class TakeMobileReaderPayment(
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    val invoiceRepository: InvoiceRepository,
    val customerRepository: CustomerRepository,
    val paymentMethodRepository: PaymentMethodRepository,
    val transactionRepository: TransactionRepository,
    var request: TransactionRequest,
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

        val voidAndFail = { exception: OmniException ->
            reader.voidTransaction(result) {
                onError(exception)
            }
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
                paymentMethodId = paymentMethod.id
                customerId = customer.id
                invoiceId = invoice.id
            }
        ) {
            voidAndFail(it)
        } ?: return@coroutineScope null

        // If we the transaction is preauth, we don't need to capture it
        if (request.preauth) {
            return@coroutineScope createdTransaction
        }

        // If the transaction is not an NMI one, then we don't need to do the auth capture step
        if (!result.source.contains("NMI")) {
            return@coroutineScope createdTransaction
        }

        val successfullyCaptured = reader.capture(createdTransaction)

        if (successfullyCaptured) {
            return@coroutineScope createdTransaction
        } else {
            // Mark Stax transaction as failed
            createdTransaction.success = false
            createdTransaction.message = "Error capturing the transaction"

            // Fail the transaction in Stax
            transactionRepository.update(createdTransaction) { }

            voidAndFail(TakeMobileReaderPaymentException("Could not capture transaction"))
            return@coroutineScope null
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