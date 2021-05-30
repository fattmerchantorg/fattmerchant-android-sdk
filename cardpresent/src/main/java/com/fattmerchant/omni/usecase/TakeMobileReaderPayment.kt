package com.fattmerchant.omni.usecase

import com.fattmerchant.android.anywherecommerce.AWCDriver
import com.fattmerchant.android.chipdna.ChipDnaDriver
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

                result.source.contains(AWCDriver().source) -> {
                    result.externalId?.let {
                        transactionMeta["awcTransactionId"] = it
                    }
                }
            }

            result.request?.lineItems?.let { transactionMeta["lineItems"] = it }
            result.request?.subtotal?.let { transactionMeta["subtotal"] = it }
            result.request?.tax?.let { transactionMeta["tax"] = it }
            result.request?.tip?.let { transactionMeta["tip"] = it }
            result.request?.memo?.let { transactionMeta["memo"] = it }
            result.request?.reference?.let { transactionMeta["reference"] = it }

            return transactionMeta
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

        val cardLastFour = try {
            result.maskedPan?.substring(result.maskedPan!!.lastIndex - 3) ?: "****"
        } catch (e: Error) {
            "****"
        }

        // Create customer
        val customer = customerRepository.create(
            Customer().apply {
                firstname = if(result.transactionSource.equals("contactless", true)) "Mobile Device" else result.cardHolderFirstName ?: "SWIPE"
                lastname = if(result.transactionSource.equals("contactless", true)) "Customer" else result.cardHolderLastName ?: "CUSTOMER"
            }
        ) {
            onError(it)
        } ?: return@coroutineScope null

        // Create a PaymentMethod
        val paymentMethod = paymentMethodRepository.create(
            PaymentMethod().apply {
                merchantId = customer.merchantId
                customerId = customer.id
                method = "card"
                cardType = result.cardType
                cardExp = result.cardExpiration
                this.cardLastFour = cardLastFour
                personName = (customer.firstname ?: "") + " " + (customer.lastname ?: "")
                tokenize = false
                paymentToken = result.paymentToken
            }
        ) {
            onError(it)
        } ?: return@coroutineScope null

        // Associate payment method and invoice with customer
        invoice.paymentMethodId = paymentMethod.id
        invoice.customerId = customer.id

        // Update invoice
        invoiceRepository.update(invoice) {
            onError(it)
        } ?: return@coroutineScope null

        // Create transaction
        val transactionMeta = transactionMetaFrom(result)

        var gatewayResponse: Map<String, Any>? = null

        result.authCode?.let {
            val responseMap = mapOf(
                "gateway_specific_response_fields" to mapOf(
                    "nmi" to mapOf(
                        "authcode" to it
                    )
                )
            )

            gatewayResponse = responseMap
        }

        transactionRepository.create(
            Transaction().apply {
                paymentMethodId = paymentMethod.id
                total = request.amount.dollarsString()
                success = result.success
                lastFour = cardLastFour
                meta = transactionMeta
                type = "charge"
                method = "card"
                source = "Android|CPSDK|${result.source}"
                if(result.source == "AWC") {
                    isRefundable = false
                    isVoidable = false
                }
                customerId = customer.id
                invoiceId = invoice.id
                response = gatewayResponse
                token = result.externalId

                if (request.preauth) {
                    preAuth = true
                    isCaptured = 0
                    isVoidable = true
                    type = "pre_auth"
                }
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