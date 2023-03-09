package com.staxpayments.api.models

/**
 * A Stax Transaction
 */
open class Transaction : Model {
    override var id: String? = null
    open var authId: String? = null
    open var childCaptures: List<Transaction>? = null
    open var createdAt: String? = null
    open var customerId: String? = null
    open var gateway: String? = null
    open var gatewayId: String? = null
    open var gatewayName: String? = null
    open var invoiceId: String? = null
    open var isCaptured: Int? = null
    open var isManual: Boolean? = null
    open var isMerchantPresent: Boolean? = null
    open var isRefundable: Boolean? = null
    open var issuerAuthCode: String? = null
    open var isVoidable: Boolean? = null
    open var isVoided: Boolean? = null
    open var lastFour: String? = null
    open var merchantId: String? = null
    open var message: String? = null
    open var meta: Any? = null
    open var method: String? = null
    open var parentAuth: String? = null
    open var paymentMethodId: String? = null
    open var preAuth: Boolean? = null
    open var receiptEmailAt: String? = null
    open var receiptSmsAt: String? = null
    open var referenceId: String? = null
    open var scheduleId: String? = null
    open var settledAt: String? = null
    open var source: String? = null
    open var sourceIp: String? = null
    open var response: Any? = null
    open var token: String? = null
    open var success: Boolean = false
    open var total: String? = null
    open var totalRefunded: String? = null
    open var type: String? = null
    open var updatedAt: String? = null
    open var userId: String? = null
    open var channel: String? = "android"
}