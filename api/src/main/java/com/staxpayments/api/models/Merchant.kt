package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Merchant(
    val id: String,
    val mid: String?,
    val status: String?,
    val subdomain: String?,
    val plan: JsonElement?,
    val options: JsonElement?,
    val processor: String?,
    val branding: JsonElement?,
    val currency: List<String>?,
    @SerialName("company_name") val companyName: String?,
    @SerialName("contact_name") val contactName: String?,
    @SerialName("contact_email") val contactEmail: String?,
    @SerialName("contact_phone") val contactPhone: String?,
    @SerialName("address_1") val address1: String?,
    @SerialName("address_2") val address2: String?,
    @SerialName("address_city") val addressCity: String?,
    @SerialName("address_state") val addressState: String?,
    @SerialName("address_zip") val addressZip: String?,
    @SerialName("hosted_payments_token") val hostedPaymentsToken: String?,
    @SerialName("gateway_type") val gatewayType: String?,
    @SerialName("product_type") val productType: String?,
    @SerialName("welcome_email_sent_at") val welcomeEmailSentAt: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("gateway_name") val gatewayName: String?,
    @SerialName("allow_ach") val doesAllowAch: Boolean?
)
