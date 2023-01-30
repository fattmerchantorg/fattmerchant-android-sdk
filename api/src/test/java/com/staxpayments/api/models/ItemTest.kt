package com.staxpayments.api.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test


class ItemTest {

    private val originalData = Item(
        id = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        userId = "b58d7eee-e68d-4d12-a1f8-62f5e71382ae",
        item = "meeseeks",
        createdAt = "2022-11-16 19:04:33",
        updatedAt = "2022-11-16 19:03:33",
        thumbnailId = null,
        details = null,
        files = null,
        deletedAt = null,
        isActive = true,
        deprecationWarning = "admin",
        isDiscount = true,
        isTaxable = true,
        code = "",
        inStock = 1,
        merchantId = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
        thumbnail = null,
        user = null,
        price = 32.0,
        meta = null,
        isService = true
    )

    private val originalString =
        """{"in_stock":1,"is_active":true,"is_discount":true,"is_service":true,"is_taxable":true,"merchant_id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","thumbnail_id":null,"updated_at":"2022-11-16 19:03:33","created_at":"2022-11-16 19:04:33","deleted_at":null,"deprecation_warning":"admin","user_id":"b58d7eee-e68d-4d12-a1f8-62f5e71382ae","code":"","details":null,"files":null,"id":"79256d84-3930-4e4b-bc71-9c6587cfc65c","item":"meeseeks","meta":null,"price":32.0,"thumbnail":null,"user":null}"""

    // id is Missing in this JSON String
    private val badString =
        """{"in_stock":1,"is_active":true,"is_discount":true,"is_service":true,"is_taxable":true,"merchant_id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","thumbnail_id":null,"updated_at":"2022-11-16 19:03:33","created_at":"2022-11-16 19:04:33","deleted_at":null,"deprecation_warning":"admin","user_id":"b58d7eee-e68d-4d12-a1f8-62f5e71382ae","code":"","details":null,"files":null,"id":"79256d84-3930-4e4b-bc71-9c6587cfc65c"}"""

    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<Item>(originalString)
        assertEquals(originalData, restored)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<Item>(badString)
        assertEquals(originalData, restored)
    }
}
