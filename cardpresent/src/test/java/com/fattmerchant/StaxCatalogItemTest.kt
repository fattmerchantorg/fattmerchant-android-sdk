package com.fattmerchant

import com.fattmerchant.android.models.StaxCatalogItem
import com.fattmerchant.android.serialization.UtcDateAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StaxCatalogItemTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(UtcDateAdapterFactory()) // Use the custom UTC Date Adapter Factory
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun staxCatalogItemJsonDeserializationAndSerializationWorksCorrectly() {
        val json = """
            {
                "id": "item123",
                "user_id": "user456",
                "merchant_id": "merchant789",
                "item": "Widget",
                "code": "W123",
                "category": "Tools",
                "details": "High quality widget",
                "is_active": true,
                "is_taxable": false,
                "is_service": false,
                "is_discount": false,
                "price": 29.99,
                "in_stock": 42,
                "meta": {"color": "red", "size": "medium"},
                "created_at": "2024-08-26 14:16:29",
                "updated_at": "2024-08-26 14:16:29"
            }
        """.trimIndent()

        val adapter = moshi.adapter(StaxCatalogItem::class.java)
        val item = adapter.fromJson(json)

        // Deserialization assertions (all fields)
        assertNotNull(item)
        item!!

        assertEquals("item123", item.id)
        assertEquals("user456", item.userId)
        assertEquals("merchant789", item.merchantId)
        assertEquals("Widget", item.item)
        assertEquals("W123", item.code)
        assertEquals("Tools", item.category)
        assertEquals("High quality widget", item.details)
        assertEquals(true, item.isActive)
        assertEquals(false, item.isTaxable)
        assertEquals(false, item.isService)
        assertEquals(false, item.isDiscount)
        assertEquals(29.99, item.price ?: 0.0, 0.001)
        assertEquals(42, item.amountInStock)
        assertEquals(mapOf("color" to "red", "size" to "medium"), item.meta)

        assertNotNull(item.createdAt)
        assertNotNull(item.updatedAt)

        // Serialize back to JSON
        val jsonOutput = adapter.toJson(item)

        //  Serialization checks
        assertTrue(jsonOutput.contains("\"id\":\"item123\""))
        assertTrue(jsonOutput.contains("\"user_id\":\"user456\""))
        assertTrue(jsonOutput.contains("\"merchant_id\":\"merchant789\""))
        assertTrue(jsonOutput.contains("\"item\":\"Widget\""))
        assertTrue(jsonOutput.contains("\"code\":\"W123\""))
        assertTrue(jsonOutput.contains("\"category\":\"Tools\""))
        assertTrue(jsonOutput.contains("\"details\":\"High quality widget\""))
        assertTrue(jsonOutput.contains("\"is_active\":true"))
        assertTrue(jsonOutput.contains("\"is_taxable\":false"))
        assertTrue(jsonOutput.contains("\"is_service\":false"))
        assertTrue(jsonOutput.contains("\"is_discount\":false"))
        assertTrue(jsonOutput.contains("\"price\":29.99"))
        assertTrue(jsonOutput.contains("\"in_stock\":42"))
        assertTrue(jsonOutput.contains("\"meta\":{\"color\":\"red\",\"size\":\"medium\"}"))

        // Confirm UTC-formatted timestamps
        assertTrue(jsonOutput.contains("\"created_at\":\"2024-08-26 14:16:29\""))
        assertTrue(jsonOutput.contains("\"updated_at\":\"2024-08-26 14:16:29\""))
    }
}
