package com.staxpayments.api.models

import kotlinx.serialization.Serializable

@Serializable
data class LineItemX(
    val details: String,
    val id: String,
    val item: String,
    val price: Int,
    val quantity: Int
)
