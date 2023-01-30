package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PaymentMethod(
    @SerialName("address_1") val address1: JsonElement?,
    @SerialName("address_2") val address2: JsonElement?,
    @SerialName("address_city") val addressCity: JsonElement?,
    @SerialName("address_country") val addressCountry: String,
    @SerialName("address_state") val addressState: JsonElement?,
    @SerialName("address_zip") val addressZip: String,
    @SerialName("bank_holder_type") val bankHolderType: JsonElement?,
    @SerialName("bank_name") val bankName: JsonElement?,
    @SerialName("bank_type") val bankType: JsonElement?,
    @SerialName("card_exp") val cardExp: String?,
    @SerialName("card_exp_datetime") val cardExpDatetime: String?,
    @SerialName("card_last_four") val cardLastFour: String?,
    @SerialName("card_type") val cardType: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("customer_id") val customerId: String?,
    @SerialName("deleted_at") val deleteAt: JsonElement?,
    @SerialName("is_default") val isDefault: Int?,
    @SerialName("merchant_id") val merchant_id: String?,
    @SerialName("person_name") val personName: String?,
    @SerialName("purged_at") val purgedAt: JsonElement?,
    @SerialName("updatedAt") val updated_at: String,
    @SerialName("user_id") val userId: String,
    val method: String?,
    val nickname: String?,
    val id: String?,
    val customer: Customer?,
)
