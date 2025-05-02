package com.fattmerchant.android.models.enums

import com.squareup.moshi.Json

enum class StaxInvoiceStatus {
    @Json(name = "VOID")
    VOID,

    @Json(name = "DELETED")
    DELETED,

    @Json(name = "DRAFT")
    DRAFT,

    @Json(name = "SENT")
    SENT,

    @Json(name = "VIEWED")
    VIEWED,

    @Json(name = "PAID")
    PAID,

    @Json(name = "PARTIAL")
    PARTIAL,

    @Json(name = "ATTEMPTED")
    ATTEMPTED
}
