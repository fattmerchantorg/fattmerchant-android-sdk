package com.staxpayments.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Size(
    val width: Int?,
    val height: Int?
)
