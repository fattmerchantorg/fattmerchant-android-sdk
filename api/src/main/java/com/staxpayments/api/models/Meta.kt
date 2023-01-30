package com.staxpayments.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val lineItems: List<LineItem>,
    val subtotal: Int,
    val tax: Int
)
