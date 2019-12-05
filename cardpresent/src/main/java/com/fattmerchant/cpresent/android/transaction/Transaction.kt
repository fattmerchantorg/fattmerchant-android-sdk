package com.fattmerchant.cpresent.android.transaction

import com.fattmerchant.cpresent.omni.entity.models.Transaction as OmniTransaction
import com.squareup.moshi.Json

class Transaction : OmniTransaction() {

    companion object {
        fun fromOmniTransaction(t: OmniTransaction): Transaction {
            return Transaction().apply {
                id = t.id
                childCaptures = t.childCaptures
                gateway = t.gateway
                gatewayName = t.gatewayName
                lastFour = t.lastFour
                message = t.message
                meta = t.meta
                method = t.method
                parentAuth = t.parentAuth
                preAuth = t.preAuth
                source = t.source
                sourceIp = t.sourceIp
                response = t.response
                spreedlyToken = t.spreedlyToken
                success = t.success
                total = t.total
                totalRefunded = t.totalRefunded
                type = t.type
                isCaptured = t.isCaptured
                isManual = t.isManual
                isMerchantPresent = t.isMerchantPresent
                isRefundable = t.isRefundable
                isVoidable = t.isVoidable
                isVoided = t.isVoided
                authId = t.authId
                customerId = t.customerId
                gatewayId = t.gatewayId
                invoiceId = t.invoiceId
                merchantId = t.merchantId
                paymentMethodId = t.paymentMethodId
                referenceId = t.referenceId
                scheduleId = t.scheduleId
                userId = t.userId
                createdAt = t.createdAt
                receiptEmailAt = t.receiptEmailAt
                receiptSmsAt = t.receiptSmsAt
                settledAt = t.settledAt
                updatedAt = t.updatedAt
            }
        }
    }

    @Json(name = "child_captures")
    override var childCaptures: Any? = null

    @Json(name = "gateway_name")
    override var gatewayName: String? = null

    @Json(name = "last_four")
    override var lastFour: String? = null

    @Json(name = "parent_auth")
    override var parentAuth: String? = null

    @Json(name = "pre_auth")
    override var preAuth: Boolean? = null

    @Json(name = "source_ip")
    override var sourceIp: String? = null

    @Json(name = "spreedly_response")
    override var response: Any? = null

    @Json(name = "spreedly_token")
    override var spreedlyToken: String? = null

    @Json(name = "total_refunded")
    override var totalRefunded: String? = null

    @Json(name = "is_captured")
    override var isCaptured: Int? = null

    @Json(name = "is_manual")
    override var isManual: Boolean? = null

    @Json(name = "is_merchant_present")
    override var isMerchantPresent: Boolean? = null

    @Json(name = "is_refundable")
    override var isRefundable: Boolean? = null

    @Json(name = "is_voidable")
    override var isVoidable: Boolean? = null

    @Json(name = "is_voided")
    override var isVoided: Boolean? = null

    @Json(name = "auth_id")
    override var authId: String? = null

    @Json(name = "customer_id")
    override var customerId: String? = null

    @Json(name = "gateway_id")
    override var gatewayId: String? = null

    @Json(name = "invoice_id")
    override var invoiceId: String? = null

    @Json(name = "merchant_id")
    override var merchantId: String? = null

    @Json(name = "paymentMethod_id")
    override var paymentMethodId: String? = null

    @Json(name = "reference_id")
    override var referenceId: String? = null

    @Json(name = "schedule_id")
    override var scheduleId: String? = null

    @Json(name = "user_id")
    override var userId: String? = null

    @Json(name = "created_at")
    override var createdAt: String? = null

    @Json(name = "receiptEmail_at")
    override var receiptEmailAt: String? = null

    @Json(name = "receiptSms_at")
    override var receiptSmsAt: String? = null

    @Json(name = "settled_at")
    override var settledAt: String? = null

    @Json(name = "updated_at")
    override var updatedAt: String? = null
}