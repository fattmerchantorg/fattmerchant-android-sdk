package com.fattmerchant.omni.data.models

import com.squareup.moshi.Json

class ChargeRequest(
    @Json(name = "payment_method_id")
    var paymentMethodId: String,
    var total: String,
    var preAuth: Boolean,
    var meta: Any? = null
)
