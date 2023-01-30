package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Item(
    @SerialName("in_stock") val inStock: Int?,
    @SerialName("is_active") val isActive: Boolean?,
    @SerialName("is_discount") val isDiscount: Boolean?,
    @SerialName("is_service") val isService: Boolean?,
    @SerialName("is_taxable") val isTaxable: Boolean?,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("thumbnail_id") val thumbnailId: JsonElement?,
    @SerialName("updated_at") val updatedAt: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("deprecation_warning") val deprecationWarning: String?,
    @SerialName("user_id") val userId: String?,
    val code: String?,
    val details: String?,
    val files: JsonElement?,
    val id: String?,
    val item: String?,
    val meta: JsonElement?,
    val price: Double?,
    val thumbnail: JsonElement?,
    val user: User?
)
