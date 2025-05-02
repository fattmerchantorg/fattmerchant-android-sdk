package com.fattmerchant.android.models.submodels

import com.squareup.moshi.Json

class StaxSurchargeReview(

    @Json(name = "bin_type")
    val binType: String? = null,

    @Json(name = "surcharge_rate")
    val surchargeRate: Double? = null,

    @Json(name = "surcharge_amount")
    val surchargeAmount: Double? = null,

    @Json(name = "total_with_surcharge_amount")
    val totalWithSurchargeAmount: Double? = null
)
