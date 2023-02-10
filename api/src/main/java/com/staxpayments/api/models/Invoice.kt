package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Invoice(
    val id: String? = null,
    val total: Double? = null,
    val meta: JsonElement? = null,
    val status: String? = null,
    val url: String? = null,
    val reminder: JsonElement? = null,
    val schedule: JsonElement? = null,
    val customer: Customer? = null,
    val user: User? = null,
    val files: JsonElement? = null,
    @SerialName("child_transactions") val childTransactions: List<Transaction>? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("merchant_id") val merchantId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("is_merchant_present") val isMerchantPresent: Boolean? = null,
    @SerialName("sent_at") val sentAt: String? = null,
    @SerialName("viewed_at") val viewedAt: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("schedule_id") val scheduleId: String? = null,
    @SerialName("reminder_id") val reminderId: String? = null,
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
    @SerialName("is_webpayment") val isWebPayment: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("is_partial_payment_enabled") val isPartialPaymentEnabled: Boolean? = null,
    @SerialName("invoice_date_at") val invoiceDateAt: String? = null,
    @SerialName("payment_attempt_failed") val paymentAttemptFailed: Boolean? = null,
    @SerialName("payment_attempt_message") val paymentAttemptMessage: String? = null,
    @SerialName("balance_due") val balanceDue: Double? = null,
    @SerialName("total_paid") val totalPaid: Double? = null,
    @SerialName("payment_meta") val paymentMeta: JsonElement? = null,
    @SerialName("send_now") val sendNow: Boolean? = null
)
