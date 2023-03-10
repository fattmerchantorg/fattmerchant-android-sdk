package com.staxpayments.sdk.data

import com.staxpayments.android.chipdna.ChipDnaDriver
import com.staxpayments.api.models.Customer
import com.staxpayments.api.models.Invoice
import com.staxpayments.api.models.PaymentMethod
import com.staxpayments.api.models.Transaction

/**
 * Represents a request for a mobile reader payment
 *
 * This includes important data like the cardholder name, amount, and auth code.
 */
open class TransactionResult {

    companion object { }

    /** The [TransactionRequest] that initiated the transaction that gave this result */
    var request: TransactionRequest? = null

    /** True when transaction succeeded, false otherwise */
    var success: Boolean? = false

    /** The place where the transaction took place. For example, "NMI" or "AWC" */
    var source: String = ""

    /**
     * The masked card number
     *
     * This is *not* the entire card number! The card number will have a mask on so only the last four digits are shown
     *
     * For example: "4111111111111111" should be "************1111"
     * */
    internal var maskedPan: String? = null

    /** The first name of the cardholder */
    var cardHolderFirstName: String? = null

    /** The last name of the cardholder */
    var cardHolderLastName: String? = null

    /** The code that authorizes the sale */
    var authCode: String? = null

    /** Sale, Refund, etc */
    var transactionType: String? = null

    /** CONTACTLESS, etc */
    var transactionSource: String? = null

    /** [Amount] of money that was exchanged */
    var amount: Amount? = null

    /** VISA, Mastercard, JCB, etc */
    var cardType: String? = null

    /** The expiration date of the card in 'mmyy' format */
    var cardExpiration: String? = null

    /** A user-defined string used to refer to the transaction */
    var userReference: String? = null

    /**
     * The ID of this transaction in the local database
     *
     * For example, in the case of NMI, this would be the CardEaseReferenceId
     */
    var localId: String? = null

    /**
     * The ID of this transaction in the 3rd party responsible for performing it.
     *
     * For example, in the case of NMI, this would be the TransactionID
     */
    var externalId: String? = null

    /** The gateway response in its entirety */
    var gatewayResponse: MutableMap<String, Any?>? = null

    var transactionMeta: MutableMap<String, Any?>? = null

    /** The token that represents this payment method */
    internal var paymentToken: String? = null

    /** A message to display with the transaction
     *
     *  In Stax, this is typically used to show an error describing what went wrong with the
     *  transaction. For example, "Insufficient Funds"
     */
    internal var message: String? = null

    internal fun customerFirstName(): String =
        if (transactionSource.equals(
                "contactless",
                true
            )
        ) "Mobile Device" else cardHolderFirstName ?: "SWIPE"

    internal fun customerLastName(): String =
        if (transactionSource.equals(
                "contactless",
                true
            )
        ) "Customer" else cardHolderLastName ?: "CUSTOMER"

    internal fun customerName(): String = "${customerFirstName()} ${customerLastName()}"

    internal fun cardLastFour() = try {
        maskedPan?.substring(maskedPan!!.lastIndex - 3) ?: "****"
    } catch (e: Error) {
        "****"
    }

    internal fun transactionMeta(): Map<String, Any?> {
        var transactionMeta = this.transactionMeta ?: mutableMapOf()

        when {
            source.contains(ChipDnaDriver().source) -> {
                userReference?.let {
                    transactionMeta["nmiUserRef"] = it
                }

                localId?.let {
                    transactionMeta["cardEaseReference"] = it
                }

                externalId?.let {
                    transactionMeta["nmiTransactionId"] = it
                }
            }
        }

        request?.lineItems?.let { transactionMeta["lineItems"] = it }
        request?.subtotal?.let { transactionMeta["subtotal"] = it }
        request?.tax?.let { transactionMeta["tax"] = it }
        request?.tip?.let { transactionMeta["tip"] = it }
        request?.memo?.let { transactionMeta["memo"] = it }
        request?.reference?.let { transactionMeta["reference"] = it }

        request?.meta?.let { passthroughMeta ->
            transactionMeta = passthroughMeta.plus(transactionMeta).toMutableMap()
        }

        return transactionMeta
    }

    internal fun invoiceMeta(): Map<String, Any> {
        val invoiceMeta = mutableMapOf<String, Any>()

        request?.lineItems?.let { invoiceMeta["lineItems"] = it }
        request?.subtotal?.let { invoiceMeta["subtotal"] = it }
        request?.tax?.let { invoiceMeta["tax"] = it }
        request?.tip?.let { invoiceMeta["tip"] = it }
        request?.memo?.let { invoiceMeta["memo"] = it }
        request?.reference?.let { invoiceMeta["reference"] = it }

        return invoiceMeta
    }

    internal fun generateTransaction(): Transaction {
        val transactionMeta = transactionMeta()

        var gatewayResponse: Map<String, Any?>? = this.gatewayResponse

        if (source.contains("nmi")) {
            authCode?.let {
                val responseMap = mapOf(
                    "gateway_specific_response_fields" to mapOf(
                        "nmi" to mapOf(
                            "authcode" to it
                        )
                    )
                )

                gatewayResponse = responseMap
            }
        }

        return Transaction().apply {
            total = amount?.dollarsString()
            success = this@TransactionResult.success == true
            lastFour = cardLastFour()
            meta = transactionMeta
            type = "charge"
            method = "card"
            source = "Android|CPSDK|${this@TransactionResult.source}"

            if (source?.contains("terminalservice.dejavoo") == true) {
                source = "terminalservice.dejavoo"
            }

            if(source == "AWC") {
                isRefundable = false
                isVoidable = false
            }
            response = gatewayResponse
            token = externalId

            if (request?.preauth == true) {
                preAuth = true
                isCaptured = 0
                isVoidable = true
                type = "pre_auth"
            }
        }
    }

    internal fun generateCustomer(): Customer {
        return Customer().apply {
            firstname = customerFirstName()
            lastname = customerLastName()
        }
    }

    internal fun generatePaymentMethod(): PaymentMethod {
        return PaymentMethod().apply {
            method = "card"
            cardType = this@TransactionResult.cardType
            cardExp = this@TransactionResult.cardExpiration
            this.cardLastFour = cardLastFour()
            personName = customerName()
            tokenize = false
            paymentToken = this@TransactionResult.paymentToken
        }
    }

    internal fun generateInvoice(): Invoice {
        return Invoice().apply {
            total = request?.amount?.dollarsString()
            url = "https://fattpay.com/#/bill/"
            meta = mapOf("subtotal" to (request?.amount?.dollarsString() ?: "0.0"))
        }
    }
}
