package com.fattmerchant.tokenization.models

import com.squareup.moshi.Json

/** A payment method such as a credit card or a bank account */
open class PaymentMethod(
    val id: String,
    val method: String,
    val nickname: String,

    @Json(name = "has_cvv")
    val hasCvv: Boolean,

    @Json(name = "person_name")
    val personName: String,

    @Json(name = "card_type")
    val cardType: String?,

    @Json(name = "card_last_four")
    val cardLastFour: String?,

    @Json(name = "card_exp")
    val cardExp: String?,

    @Json(name = "bank_name")
    val bankName: String?,

    @Json(name = "bank_type")
    val bankType: String?,

    @Json(name = "bank_holder_type")
    val bankHolderType: String?
)
