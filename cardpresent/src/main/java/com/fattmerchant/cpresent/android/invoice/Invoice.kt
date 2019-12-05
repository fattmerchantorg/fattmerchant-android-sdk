package com.fattmerchant.cpresent.android.invoice

import com.fattmerchant.cpresent.omni.entity.models.Invoice as OmniInvoice
import com.squareup.moshi.Json

class Invoice: OmniInvoice() {

    companion object {
        fun fromOmniInvoice(i: OmniInvoice): Invoice {
            return Invoice().apply {
                balanceDue = i.balanceDue
                createdAt = i.createdAt
                customerId = i.customerId
                deletedAt = i.deletedAt
                dueAt = i.dueAt
                id = i.id
                isMerchantPresent = i.isMerchantPresent
                isPartialPaymentEnabled = i.isPartialPaymentEnabled
                isWebpayment = i.isWebpayment
                merchantId = i.merchantId
                meta = i.meta
                paidAt = i.paidAt
                paymentAttemptFailed = i.paymentAttemptFailed
                paymentAttemptMessage = i.paymentAttemptMessage
                paymentMeta = i.paymentMeta
                paymentMethodId = i.paymentMethodId
                scheduleId = i.scheduleId
                sentAt = i.sentAt
                status = i.status
                total = i.total
                totalPaid = i.totalPaid
                updatedAt = i.updatedAt
                url = i.url
                userId = i.userId
                viewedAt = i.viewedAt
            }
        }
    }

    @Json(name = "payment_attempt_failed")
    override var paymentAttemptFailed: Boolean? = null

    @Json(name = "payment_attempt_message")
    override var paymentAttemptMessage: String? = null

    @Json(name = "balance_due")
    override var balanceDue: String? = null

    @Json(name = "total_paid")
    override var totalPaid: String? = null

    @Json(name = "payment_meta")
    override var paymentMeta: Any? = null

    @Json(name = "user_id")
    override var userId: String? = null

    @Json(name = "customer_id")
    override var customerId: String? = null

    @Json(name = "merchant_id")
    override var merchantId: String? = null

    @Json(name = "payment_method_id")
    override var paymentMethodId: String? = null

    @Json(name = "schedule_id")
    override var scheduleId: String? = null

    @Json(name = "is_merchant_present")
    override var isMerchantPresent: Boolean? = null

    @Json(name = "is_partial_payment_enabled")
    override var isPartialPaymentEnabled: Boolean? = null

    @Json(name = "is_webpayment")
    override var isWebpayment: Boolean? = null

    @Json(name = "created_at")
    override var createdAt: String? = null

    @Json(name = "deleted_at")
    override var deletedAt: String? = null

    @Json(name = "due_at")
    override var dueAt: String? = null

    @Json(name = "paid_at")
    override var paidAt: String? = null

    @Json(name = "sent_at")
    override var sentAt: String? = null

    @Json(name = "updated_at")
    override var updatedAt: String? = null

    @Json(name = "viewed_at")
    override var viewedAt: String? = null
}