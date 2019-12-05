package com.fattmerchant.cpresent.android.payment_method

import com.fattmerchant.cpresent.omni.entity.models.PaymentMethod as OmniPaymentMethod
import com.squareup.moshi.Json

class PaymentMethod : OmniPaymentMethod() {

    companion object {
        fun fromOmniPaymentMethod(p: OmniPaymentMethod): PaymentMethod {
            return PaymentMethod().apply {
                cardExpDatetime = p.cardExpDatetime
                personName = p.personName
                spreedlyToken = p.spreedlyToken
                address1 = p.address1
                address2 = p.address2
                addressCity = p.addressCity
                addressCountry = p.addressCountry
                addressState = p.addressState
                addressZip = p.addressZip
                bankHolderType = p.bankHolderType
                bankName = p.bankName
                bankType = p.bankType
                cardExp = p.cardExp
                cardLastFour = p.cardLastFour
                cardType = p.cardType
                customerId = p.customerId
                merchantId = p.merchantId
                hasCvv = p.hasCvv
                isDefault = p.isDefault
                isUsableInVt = p.isUsableInVt
                createdAt = p.createdAt
                deletedAt = p.deletedAt
                purgedAt = p.purgedAt
                updatedAt = p.updatedAt
                tokenize = p.tokenize
                method = p.method
            }
        }
    }

    @Json(name = "card_exp_datetime")
    override var cardExpDatetime: Any? = null // TODO: Make this a string

    @Json(name = "person_name")
    override var personName: String? = null

    @Json(name = "spreedly_token")
    override var spreedlyToken: String? = null

    @Json(name = "address_1")
    override var address1: String? = null

    @Json(name = "address_2")
    override var address2: String? = null

    @Json(name = "address_city")
    override var addressCity: String? = null

    @Json(name = "address_country")
    override var addressCountry: String? = null

    @Json(name = "address_state")
    override var addressState: String? = null

    @Json(name = "address_zip")
    override var addressZip: String? = null

    @Json(name = "bank_holder_type")
    override var bankHolderType: String? = null

    @Json(name = "bank_name")
    override var bankName: String? = null

    @Json(name = "bank_type")
    override var bankType: String? = null

    @Json(name = "card_exp")
    override var cardExp: String? = null

    @Json(name = "card_last_four")
    override var cardLastFour: String? = null

    @Json(name = "card_type")
    override var cardType: String? = null

    @Json(name = "customer_id")
    override var customerId: String? = null

    @Json(name = "merchant_id")
    override var merchantId: String? = null

    @Json(name = "has_cvv")
    override var hasCvv: Boolean? = null

    @Json(name = "is_default")
    override var isDefault: Int? = null //TODO: Make this a boolean and check for int in the json parsing

    @Json(name = "is_usable_in_vt")
    override var isUsableInVt: Boolean? = null

    @Json(name = "created_at")
    override var createdAt: String? = null

    @Json(name = "deleted_at")
    override var deletedAt: String? = null

    @Json(name = "purged_at")
    override var purgedAt: String? = null

    @Json(name = "updated_at")
    override var updatedAt: String? = null
}