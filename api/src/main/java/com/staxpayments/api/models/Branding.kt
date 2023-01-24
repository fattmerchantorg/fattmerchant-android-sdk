package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Branding(
    val id: String,
    val name: String?,
    val tag: String?,
    val meta: Meta?,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("user_id") val userId: String?,
    @SerialName("public_url") val publicUrl: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String?
)
