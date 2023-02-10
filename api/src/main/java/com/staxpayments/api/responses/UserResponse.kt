package com.staxpayments.api.responses

import com.staxpayments.api.models.Merchant
import com.staxpayments.api.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val user: User?,
    val merchant: Merchant?
)
