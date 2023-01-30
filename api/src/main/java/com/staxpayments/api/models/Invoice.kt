package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Invoice(
    val id: String,
    val total: Double?,
    val meta: JsonElement?,
    val status: String?,
    val url: String?,
    val reminder: JsonElement?,
    val schedule: JsonElement?,
    val customer: Customer?,
    val user: User?,
    val files: JsonElement?,
    @SerialName("child_transactions") val childTransactions: List<Transaction>?,
    @SerialName("customer_id") val customerId: String?,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("user_id") val userId: String?,
    @SerialName("is_merchant_present") val isMerchantPresent: Boolean?,
    @SerialName("sent_at") val sentAt: String?,
    @SerialName("viewed_at") val viewedAt: String?,
    @SerialName("paid_at") val paidAt: String?,
    @SerialName("schedule_id") val scheduleId: String?,
    @SerialName("reminder_id") val reminderId: String?,
    @SerialName("payment_method_id") val paymentMethodId: String?,
    @SerialName("is_webpayment") val isWebPayment: Boolean?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("due_at") val dueAt: String?,
    @SerialName("is_partial_payment_enabled") val isPartialPaymentEnabled: Boolean?,
    @SerialName("invoice_date_at") val invoiceDateAt: String?,
    @SerialName("payment_attempt_failed") val paymentAttemptFailed: Boolean?,
    @SerialName("payment_attempt_message") val paymentAttemptMessage: String?,
    @SerialName("balance_due") val balanceDue: Double,
    @SerialName("total_paid") val totalPaid: Double,
    @SerialName("payment_meta") val paymentMeta: JsonElement?
)
