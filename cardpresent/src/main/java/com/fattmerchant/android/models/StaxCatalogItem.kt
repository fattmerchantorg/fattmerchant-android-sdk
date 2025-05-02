package com.fattmerchant.android.models

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

class StaxCatalogItem(

    @Json(name = "id")
    var id: String? = null,

    @Json(name = "user_id")
    var userId: String? = null,

    @Json(name = "merchant_id")
    var merchantId: String? = null,

    @Json(name = "item")
    var item: String? = null,

    @Json(name = "code")
    var code: String? = null,

    @Json(name = "category")
    var category: String? = null,

    @Json(name = "details")
    var details: String? = null,

    @Json(name = "is_active")
    var isActive: Boolean? = null,

    @Json(name = "is_taxable")
    var isTaxable: Boolean? = null,

    @Json(name = "is_service")
    var isService: Boolean? = null,

    @Json(name = "is_discount")
    var isDiscount: Boolean? = null,

    @Json(name = "price")
    var price: Double? = null,

    @Json(name = "in_stock")
    var amountInStock: Int? = null,

    @Json(name = "meta")
    var meta: Map<String, Any>? = null,

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

    class Update(private val item: StaxCatalogItem) {
        private val changes = mutableMapOf<String, Any?>()

        fun set(field: String, value: Any?): Update {
            changes[field] = value
            return this
        }

        fun modifiedFields(): Map<String, Any?> = changes

        fun apply(): StaxCatalogItem {
            return StaxCatalogItem(
                id = item.id,
                userId = item.userId,
                merchantId = item.merchantId,
                item = changes["item"] as? String ?: item.item,
                code = changes["code"] as? String ?: item.code,
                category = changes["category"] as? String ?: item.category,
                details = changes["details"] as? String ?: item.details,
                isActive = changes["isActive"] as? Boolean ?: item.isActive,
                isTaxable = changes["isTaxable"] as? Boolean ?: item.isTaxable,
                isService = changes["isService"] as? Boolean ?: item.isService,
                isDiscount = changes["isDiscount"] as? Boolean ?: item.isDiscount,
                price = changes["price"] as? Double ?: item.price,
                amountInStock = changes["amountInStock"] as? Int ?: item.amountInStock,
                meta = safeMapCast(changes["meta"]) ?: item.meta,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}
