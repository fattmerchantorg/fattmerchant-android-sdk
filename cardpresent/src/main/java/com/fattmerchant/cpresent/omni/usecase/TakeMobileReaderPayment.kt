package com.fattmerchant.cpresent.omni.usecase

import com.fattmerchant.cpresent.omni.entity.*
import com.fattmerchant.cpresent.omni.entity.models.Customer
import com.fattmerchant.cpresent.omni.entity.models.Invoice
import com.fattmerchant.cpresent.omni.entity.models.PaymentMethod
import com.fattmerchant.cpresent.omni.entity.models.Transaction
import com.fattmerchant.cpresent.omni.entity.repository.*
import com.fattmerchant.cpresent.omni.networking.JsonParser
import com.fattmerchant.cpresent.omni.networking.OmniApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

class TakeMobileReaderPayment(
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    val invoiceRepository: InvoiceRepository,
    val customerRepository: CustomerRepository,
    val paymentMethodRepository: PaymentMethodRepository,
    val transactionRepository: TransactionRepository,
    val request: TransactionRequest,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(onError: (Error) -> Unit): Transaction? = coroutineScope {

        // Get the reader responsible for taking the payment
        val reader = getAvailableMobileReaderDriver(mobileReaderDriverRepository)
        if (reader == null) {
            onError(Error("No available mobile reader"))
            return@coroutineScope null
        }

        // Create invoice
        val invoice: Invoice = invoiceRepository.create(
            Invoice().apply {
                total = request.amount.dollarsString()
                url = "https://qa.fattpay.com/#/bill/"
                meta = mapOf("subtotal" to request.amount.dollarsString())
            }
        ) {
            onError(it)
        } ?: return@coroutineScope null

        // Take the mobile reader payment
        val result: TransactionResult

        try {
            result = reader.performTransaction(request)
        } catch (e: Error) {
            // Something went wrong while performing the transaction
            onError(e)
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
                firstname = result.cardHolderFirstName
                lastname = result.cardHolderLastName
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
                this.cardLastFour = cardLastFour
                personName = (customer.firstname ?: "") + " " + (customer.lastname ?: "")
                tokenize = false
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
        val transactionMeta = mutableMapOf<String, Any>()
        transactionMeta["nmiUserRef"] = result.userReference ?: ""

        var spreedlyResponse: Map<String, Any>? = null

        result.authCode?.let {
            val responseMap = mapOf(
                "gateway_specific_response_fields" to mapOf(
                    "nmi" to mapOf(
                        "authcode" to it
                    )
                )
            )

            spreedlyResponse = responseMap
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
                source = "Android|CPSDK"
                customerId = customer.id
                invoiceId = invoice.id
                response = spreedlyResponse
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
        .getDrivers()
        .firstOrNull { it.isReadyToTakePayment() }

}