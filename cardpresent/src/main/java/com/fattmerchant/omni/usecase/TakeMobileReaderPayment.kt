package com.fattmerchant.omni.usecase

import com.fattmerchant.android.chipdna.ChipDnaDriver
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionResult
import com.fattmerchant.omni.data.models.Customer
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.CustomerRepository
import com.fattmerchant.omni.data.repository.InvoiceRepository
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import com.fattmerchant.omni.data.repository.PaymentMethodRepository
import com.fattmerchant.omni.data.repository.TransactionRepository
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

    companion object {
        internal fun transactionMetaFrom(result: TransactionResult): Map<String, Any> {
            val transactionMeta = mutableMapOf<String, Any>()

            when {
                result.source.contains(ChipDnaDriver().source) -> {
                    result.userReference?.let {
                        transactionMeta["nmiUserRef"] = it
                    }

                    result.localId?.let {
                        transactionMeta["cardEaseReference"] = it
                    }

                    result.externalId?.let {
                        transactionMeta["nmiTransactionId"] = it
                    }
                }
            }

            result.request?.lineItems?.let { transactionMeta["lineItems"] = it }
            result.request?.subtotal?.let { transactionMeta["subtotal"] = it }
            result.request?.tax?.let { transactionMeta["tax"] = it }
            result.request?.tip?.let { transactionMeta["tip"] = it }
            result.request?.memo?.let { transactionMeta["memo"] = it }
            result.request?.reference?.let { transactionMeta["reference"] = it }
            result.request?.shippingAmount?.let { transactionMeta["shippingAmount"] = it }
            result.request?.poNumber?.let { transactionMeta["poNumber"] = it }

            return transactionMeta
        }

        internal fun invoiceMetaFrom(result: TransactionResult): Map<String, Any> {
            val invoiceMeta = mutableMapOf<String, Any>()

            result.request?.lineItems?.let { invoiceMeta["lineItems"] = it }
            result.request?.subtotal?.let { invoiceMeta["subtotal"] = it }
            result.request?.tax?.let { invoiceMeta["tax"] = it }
            result.request?.tip?.let { invoiceMeta["tip"] = it }
            result.request?.memo?.let { invoiceMeta["memo"] = it }
            result.request?.reference?.let { invoiceMeta["reference"] = it }
            result.request?.shippingAmount?.let { invoiceMeta["shippingAmount"] = it }
            result.request?.poNumber?.let { invoiceMeta["poNumber"] = it }

            return invoiceMeta
        }
    }

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