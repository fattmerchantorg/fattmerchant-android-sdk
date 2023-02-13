package com.staxpayments.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val lineItems: List<LineItemX>,
    val poNumber: String,
    val shippingAmount: Int,
    val subtotal: Int,
    val tax: Int
)
