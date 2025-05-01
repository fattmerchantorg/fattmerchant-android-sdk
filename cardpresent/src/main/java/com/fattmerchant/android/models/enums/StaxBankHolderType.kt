package com.fattmerchant.android.models.enums

import com.squareup.moshi.Json

enum class StaxBankHolderType {
    @Json(name = "business")
    BUSINESS,

    @Json(name = "personal")
    PERSONAL
}
