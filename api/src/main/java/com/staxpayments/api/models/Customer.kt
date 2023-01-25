package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    @SerialName("firstname") val firstName : String?,
    @SerialName("address_1") val fistAddress: String?,
    @SerialName("address_2") val secondAddress: String?,
    @SerialName("address_city") val addressCity: String?,
    @SerialName("address_country") val addressCountry: String?,
    @SerialName("address_state") val addressState: String?,
    @SerialName("address_zip") val address_zip: String?,
    @SerialName("cc_emails") val CCemails: List<String>?,
    @SerialName("cc_sms") val CCsms: Any?,
    @SerialName("has_address") val hasAddress: Boolean,
    @SerialName("missingAddressComponents")
    val missing_address_components: List<String>,
    @SerialName("allow_invoice_credit_card_payments")
    val allowInvoiceCreditCardPayments: Boolean,
    @SerialName("updated_at") val updatedAt: String?
    @SerialName("created_at") val createdAt: String?,
    @SerialName("deleted_at") val deletedAt: String?,
    val email: String?,
    val gravatar: String?,
    val company: String?,
    val lastname: String?,
    val notes: String?,
    val options: Options,
    val phone: String?,
    val reference: String?,

)