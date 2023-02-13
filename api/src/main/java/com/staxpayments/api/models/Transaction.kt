package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class Transaction(
    @SerialName("child_transactions") val childTransactions: JsonElement?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("customer_id") val customerId: String,
    @SerialName("invoice_id") val invoiceId: String?,
    @SerialName("is_manual") val isManual: JsonElement?,
    @SerialName("is_refundable") val isRefundable: Boolean?,
    @SerialName("is_voidable") val isVoidable: Boolean?,
    @SerialName("is_voided") val isVoided: Boolean?,
    @SerialName("last_four") val lastFour: String?,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("payment_method") val paymentMethod: PaymentMethod?,
    @SerialName("payment_method_id") val paymentMethodId: String?,
    @SerialName("pre_auth") val preAuth: Boolean?,
    @SerialName("receipt_email_at") val receiptEmailAt: JsonElement?,
    @SerialName("receipt_sms_at") val receiptSmsAt: JsonElement?,
    @SerialName("recurring_transaction_id") val recurringTransactionId: String?,
    @SerialName("reference_id") val referenceId: String?,
    @SerialName("schedule_id") val scheduleId: JsonElement?,
    @SerialName("total_refunded") val totalRefunded: JsonElement?,
    @SerialName("updated_at") val updatedAt: String?,
    @SerialName("user_id") val userId: String?,
    val user: User?,
    val type: String?,
    val source: JsonElement?,
    val success: Boolean?,
    val total: Int?,
    val message: JsonElement?,
    val meta: Meta?,
    val method: String?,
    val files: JsonElement?,
    val id: String,
    val currency: String?,
    val customer: Customer?,
)
