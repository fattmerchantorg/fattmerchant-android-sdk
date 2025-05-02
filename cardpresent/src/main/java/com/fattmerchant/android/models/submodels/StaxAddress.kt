package com.fattmerchant.android.models.submodels

import com.squareup.moshi.Json

class StaxAddress(

    @Json(name = "line1")
    val line1: String? = null,

    @Json(name = "line2")
    val line2: String? = null,

    @Json(name = "city")
    val city: String? = null,

    @Json(name = "state")
    val state: String? = null,

    @Json(name = "zip")
    val zip: String? = null,

    @Json(name = "country")
    val country: String? = null
)
