package com.fattmerchant.android.models

import com.fattmerchant.android.models.enums.StaxInvoiceStatus
import com.fattmerchant.android.serialization.Utc
import com.squareup.moshi.Json
import java.util.Date

class StaxInvoice(

    @Json(name = "id")
    val id: String? = null,

    @Json(name = "user_id")
    val userId: String? = null,

    @Json(name = "customer_id")
    val customerId: String? = null,

    @Json(name = "merchant_id")
    val merchantId: String? = null,

    @Json(name = "payment_method_id")
    val paymentMethodId: String? = null,

    @Json(name = "schedule_id")
    val scheduleId: String? = null,

    @Json(name = "balance_due")
    val balanceDue: Double? = null,

    @Json(name = "is_merchant_present")
    val isMerchantPresent: Boolean? = null,

    @Json(name = "is_webpayment")
    val isWebpayment: Boolean? = null,

    @Json(name = "payment_attempt_failed")
    val hasPaymentAttemptFailed: Boolean? = null,

    @Json(name = "payment_attempt_message")
    val paymentAttemptMessage: String? = null,

    @Json(name = "status")
    val status: StaxInvoiceStatus? = null,

    @Json(name = "total")
    val total: Double? = null,

    @Json(name = "total_paid")
    val totalPaid: Double? = null,

    @Json(name = "url")
    val url: String? = null,

    @Json(name = "meta")
    val meta: Map<String, Any>? = null,

    @Json(name = "due_at")
    @Utc
    val dueAt: Date? = null,

    @Json(name = "sent_at")
    @Utc
    val sentAt: Date? = null,

    @Json(name = "paid_at")
    @Utc
    val paidAt: Date? = null,

    @Json(name = "viewed_at")
    @Utc
    val viewedAt: Date? = null,

    @Json(name = "created_at")
    @Utc
    val createdAt: Date? = null,

    @Json(name = "updated_at")
    @Utc
    val updatedAt: Date? = null
) {

    fun updating(): Update {
        return Update(this)
    }

    class Update(private val original: StaxInvoice) {
        private val changes = mutableMapOf<String, Any?>()

        fun set(field: String, value: Any?): Update {
            changes[field] = value
            return this
        }

        fun modifiedFields(): Map<String, Any?> = changes

        fun apply(): StaxInvoice {
            return StaxInvoice(
                id = original.id,
                userId = changes["userId"] as? String ?: original.userId,
                customerId = changes["customerId"] as? String ?: original.customerId,
                merchantId = changes["merchantId"] as? String ?: original.merchantId,
                paymentMethodId = changes["paymentMethodId"] as? String ?: original.paymentMethodId,
                scheduleId = changes["scheduleId"] as? String ?: original.scheduleId,
                balanceDue = changes["balanceDue"] as? Double ?: original.balanceDue,
                isMerchantPresent = changes["isMerchantPresent"] as? Boolean ?: original.isMerchantPresent,
                isWebpayment = changes["isWebpayment"] as? Boolean ?: original.isWebpayment,
                hasPaymentAttemptFailed = changes["hasPaymentAttemptFailed"] as? Boolean ?: original.hasPaymentAttemptFailed,
                paymentAttemptMessage = changes["paymentAttemptMessage"] as? String ?: original.paymentAttemptMessage,
                status = changes["status"] as? StaxInvoiceStatus ?: original.status,
                total = changes["total"] as? Double ?: original.total,
                totalPaid = changes["totalPaid"] as? Double ?: original.totalPaid,
                url = changes["url"] as? String ?: original.url,
                meta = safeMapCast(changes["meta"]) ?: original.meta,
                dueAt = changes["dueAt"] as? Date ?: original.dueAt,
                sentAt = original.sentAt,
                paidAt = original.paidAt,
                viewedAt = original.viewedAt,
                createdAt = original.createdAt,
                updatedAt = original.updatedAt
            )
        }
    }
}
private fun safeMapCast(value: Any?): Map<String, Any>? {
    return if (value is Map<*, *>) {
        value.entries
            .filter { it.key is String }
            .associate { it.key as String to it.value as Any }
    } else {
        null
    }
}
