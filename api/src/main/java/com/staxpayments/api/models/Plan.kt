package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Plan(
    val id: String,
    val name: String?,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("user_id") val userId: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)
