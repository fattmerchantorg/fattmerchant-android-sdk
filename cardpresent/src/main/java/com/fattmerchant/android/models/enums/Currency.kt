package com.fattmerchant.android.models.enums

import com.squareup.moshi.Json

enum class Currency {
    @Json(name = "USD") USD,
    @Json(name = "CAD") CAD,
    @Json(name = "MXN") MXN,
    @Json(name = "EUR") EUR,
    @Json(name = "GBP") GBP
}