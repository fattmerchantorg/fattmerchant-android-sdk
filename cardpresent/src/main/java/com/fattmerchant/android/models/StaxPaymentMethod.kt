package com.fattmerchant.android.models

import com.fattmerchant.android.models.enums.StaxBankAccountType
import com.fattmerchant.android.models.enums.StaxBankHolderType
import com.fattmerchant.android.models.enums.StaxPaymentMethodType
import com.squareup.moshi.Json
import com.fattmerchant.android.serialization.Utc
import java.util.Date

private fun safeMapCast(value: Any?): Map<String, Any>? {
    return if (value is Map<*, *>) {
        value.entries
            .filter { it.key is String }
            .associate { it.key as String to it.value as Any }
    } else {
        null
    }
}

class StaxPaymentMethod(

    @Json(name = "id")
    var id: String? = null,

    @Json(name = "customer_id")
    var customerId: String? = null,

    @Json(name = "merchant_id")
    var merchantId: String? = null,

    @Json(name = "user_id")
    var userId: String? = null,

    @Json(name = "nickname")
    var nickname: String? = null,

    @Json(name = "is_default")
    var isDefault: Int? = null,

    @Json(name = "method")
    var method: StaxPaymentMethodType? = null,

    @Json(name = "meta")
    var meta: Map<String, Any>? = null,

    @Json(name = "person_name")
    var personName: String? = null,

    @Json(name = "card_type")
    var cardType: String? = null,

    @Json(name = "card_last_four")
    var cardLastFour: String? = null,

    @Json(name = "card_exp")
    var cardExpiry: String? = null,

    @Json(name = "bank_note")
    var bankName: String? = null,

    @Json(name = "bank_type")
    var bankType: StaxBankAccountType? = null,

    @Json(name = "bank_holder_type")
    var bankHolderType: StaxBankHolderType? = null,

    @Json(name = "address_1")
    var address1: String? = null,

    @Json(name = "address_2")
    var address2: String? = null,

    @Json(name = "address_city")
    var addressCity: String? = null,

    @Json(name = "address_state")
    var addressState: String? = null,

    @Json(name = "address_zip")
    var addressZip: String? = null,

    @Json(name = "address_country")
    var addressCountry: String? = null,

    @Json(name = "purged_at")
    @Utc
    var purgedAt: Date? = null,

    @Json(name = "created_at")
    @Utc
    var createdAt: Date? = null,

    @Json(name = "updated_at")
    @Utc
    var updatedAt: Date? = null
) {

    fun updating(): Update {
        return Update(this)
    }

    class Update(private val item: StaxPaymentMethod) {
        private val changes = mutableMapOf<String, Any?>()

        fun set(field: String, value: Any?): Update {
            changes[field] = value
            return this
        }

        fun modifiedFields(): Map<String, Any?> = changes

        fun apply(): StaxPaymentMethod {
            return StaxPaymentMethod(
                id = item.id,
                customerId = item.customerId,
                merchantId = item.merchantId,
                userId = item.userId,
                nickname = changes["nickname"] as? String ?: item.nickname,
                isDefault = changes["isDefault"] as? Int ?: item.isDefault,
                method = item.method,
                meta = safeMapCast(changes["meta"]) ?: item.meta,
                personName = changes["personName"] as? String ?: item.personName,
                cardType = changes["cardType"] as? String ?: item.cardType,
                cardLastFour = changes["cardLastFour"] as? String ?: item.cardLastFour,
                cardExpiry = changes["cardExpiry"] as? String ?: item.cardExpiry,
                bankName = changes["bankName"] as? String ?: item.bankName,
                bankType = changes["bankType"] as? StaxBankAccountType ?: item.bankType,
                bankHolderType = changes["bankHolderType"] as? StaxBankHolderType ?: item.bankHolderType,
                address1 = changes["address1"] as? String ?: item.address1,
                address2 = changes["address2"] as? String ?: item.address2,
                addressCity = changes["addressCity"] as? String ?: item.addressCity,
                addressState = changes["addressState"] as? String ?: item.addressState,
                addressZip = changes["addressZip"] as? String ?: item.addressZip,
                addressCountry = changes["addressCountry"] as? String ?: item.addressCountry,
                purgedAt = item.purgedAt,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}
