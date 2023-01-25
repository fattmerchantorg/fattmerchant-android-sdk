package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("email_verification_sent_at") val emailVerificationSentAt: String?,
    @SerialName("email_verified_at") val emailVerifiedAt: String?,
    @SerialName("is_default") val isDefault: Boolean?,
    @SerialName("is_api_key") val isApiKey: Boolean?,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("system_admin") val systemAdmin: Boolean?,
    @SerialName("team_role") val teamRole: String?,
    @SerialName("team_admin") val teamAdmin: Boolean?,
    @SerialName("team_enabled") val teamEnabled: Boolean?,
    @SerialName("mfa_enabled") val mfaEnabled: Boolean?,
    @SerialName("merchant_options") val merchantOptions: List<String>?,
    val gravatar: String?,
    val acknowledgments: JsonElement?,
    val options: JsonElement?,
    val brand: String?
)
