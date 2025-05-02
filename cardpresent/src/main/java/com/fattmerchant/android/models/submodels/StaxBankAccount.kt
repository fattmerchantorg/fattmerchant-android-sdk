package com.fattmerchant.android.models.submodels

import com.squareup.moshi.Json

class StaxBankAccount(

    @Json(name = "name")
    val name: String,

    @Json(name = "number")
    val number: String,

    @Json(name = "expiry")
    val expiry: String,

    @Json(name = "cvv")
    val cvv: String? = null,

    @Json(name = "address")
    val address: StaxAddress? = null
)
