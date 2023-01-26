package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PaymentMethod(
    val id: String,
    val nickname: String?,
    val method: String?,
    val meta: JsonElement?,
    val customer: Customer?,
    @SerialName("customer_id") val customerId: String?,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("user_id") val userId: String?,
    @SerialName("is_default") val isDefault: Int?,
    @SerialName("bin_type") val binType: String?,
    @SerialName("person_name") val personName: String?,
    @SerialName("card_type") val cardType: String?,
    @SerialName("card_last_four") val cardLastFour: String?,
    @SerialName("card_exp") val cardExpiry: String?,
    @SerialName("bank_name") val bankName: String?,
    @SerialName("bank_type") val bankType: String?,
    @SerialName("bank_holder_type") val bankHolderType: String?,
    @SerialName("address_1") val address1: String?,
    @SerialName("address_2") val address2: String?,
    @SerialName("address_city") val addressCity: String?,
    @SerialName("address_state") val addressState: String?,
    @SerialName("address_zip") val addressZip: String?,
    @SerialName("address_country") val addressCountry: String?,
    @SerialName("purged_at") val purgedAt: String?,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("card_exp_datetime") val cardExpiryDateTime: String?
)
