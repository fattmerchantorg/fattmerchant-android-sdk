package com.staxpayments.api.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateInvoiceBody(
    val total: Double,
    val url: String?,
    val meta: JsonElement?,
    val files: JsonElement?,
    @SerialName("customer_id") val customerId: String?,
    @SerialName("send_now") val sendNow: Boolean?,
    @SerialName("is_partial_payment_enabled") val isPartialPaymentEnabled: Boolean?
)
