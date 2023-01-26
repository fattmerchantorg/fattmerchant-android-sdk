package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Customer(
    val id: String,
    @SerialName("firstname") val firstName: String?,
    @SerialName("lastname") val lastName: String?,
    @SerialName("address_1") val address1: String?,
    @SerialName("address_2") val address2: String?,
    @SerialName("address_city") val addressCity: String?,
    @SerialName("address_country") val addressCountry: String?,
    @SerialName("address_state") val addressState: String?,
    @SerialName("address_zip") val addressZip: String?,
    @SerialName("cc_emails") val CCemails: List<String>?,
    @SerialName("cc_sms") val CCsms: JsonElement?,
    @SerialName("has_address") val hasAddress: Boolean,
    @SerialName("missing_address_components")
    val missingAddressComponents: List<String>?,
    @SerialName("allow_invoice_credit_card_payments")
    val doesAllowInvoiceCreditCardPayments: Boolean,
    @SerialName("updated_at") val updatedAt: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("deleted_at") val deletedAt: String?,
    val email: String?,
    val gravatar: String?,
    val company: String?,
    val notes: String?,
    val options: JsonElement?,
    val phone: String?,
    val reference: String?,

)
