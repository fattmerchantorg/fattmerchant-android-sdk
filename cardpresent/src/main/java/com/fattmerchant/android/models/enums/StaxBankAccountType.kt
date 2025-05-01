package com.fattmerchant.android.models.enums

import com.squareup.moshi.Json

enum class StaxBankAccountType {
    @Json(name = "savings")
    SAVINGS,

    @Json(name = "checking")
    CHECKING
}
