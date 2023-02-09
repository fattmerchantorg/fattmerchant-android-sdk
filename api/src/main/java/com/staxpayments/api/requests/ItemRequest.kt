package com.staxpayments.api.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ItemRequest(
    val item: String?,
    val code: String?,
    val details: String?,
    val category: String?,
    @SerialName("is_active") val isActive: Boolean?,
    @SerialName("is_taxable") val isTaxable: Boolean?,
    @SerialName("is_service") val isService: Boolean?,
    @SerialName("is_discount") val isDiscount: Boolean?,
    val price: Double?,
    @SerialName("in_stock") val inStock: Int?,
    @SerialName("low_stock_alert") val lowStockAlert: String?,
    val meta: JsonElement?,
)
