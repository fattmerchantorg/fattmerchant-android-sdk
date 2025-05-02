package com.fattmerchant.android.models.enums

import com.squareup.moshi.Json

enum class TransactionType {
    @Json(name = "charge") CHARGE,
    @Json(name = "void") VOID,
    @Json(name = "refund") REFUND,
    @Json(name = "capture") CAPTURE,
    @Json(name = "pre_auth") PRE_AUTH,
    @Json(name = "credit") CREDIT,
    @Json(name = "") EMPTY
}