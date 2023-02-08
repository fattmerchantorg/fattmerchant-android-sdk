package com.staxpayments.api.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerRequest(
    @SerialName("firstname") val firstName: String,
    @SerialName("lastname") val lastName: String,
    val company: String?,
    val email: String?,
    @SerialName("cc_emails") val CCEmails: List<String>?,
    val phone: String?,
    @SerialName("address_1") val address1: String?,
    @SerialName("address_2") val address2: String?,
    @SerialName("address_city") val addressCity: String?,
    @SerialName("address_country") val addressCountry: String?,
    @SerialName("address_state") val addressState: String?,
    @SerialName("address_zip") val addressZip: String?,
    val reference: String?,
)
