package com.fattmerchant.android.models.enums

import com.squareup.moshi.Json

enum class StaxPaymentMethodType {
    @Json(name = "bank")
    BANK,

    @Json(name = "card")
    CARD
}