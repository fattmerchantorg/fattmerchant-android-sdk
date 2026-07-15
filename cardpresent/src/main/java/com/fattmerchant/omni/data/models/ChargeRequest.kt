package com.fattmerchant.omni.data.models

import com.google.gson.annotations.SerializedName

class ChargeRequest(
    @SerializedName("payment_method_id")
    var paymentMethodId: String,
    var total: String,
    var preAuth: Boolean,
    var meta: Any? = null
)
