package com.fattmerchant

import com.fattmerchant.android.models.StaxPaymentMethod
import com.fattmerchant.android.models.enums.StaxBankAccountType
import com.fattmerchant.android.models.enums.StaxBankHolderType
import com.fattmerchant.android.models.enums.StaxPaymentMethodType
import com.fattmerchant.android.serialization.UtcDateAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StaxPaymentMethodTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(UtcDateAdapterFactory())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun staxPaymentMethodJsonDeserializationAndSerializationWorksCorrectly() {
        val json = """
            {
                "id": "pm123",
                "customer_id": "cust456",
                "merchant_id": "merchant789",
                "user_id": "user321",
                "nickname": "Primary Card",
                "is_default": 1,
                "method": "card",
                "meta": {"verified": true, "issuer": "Chase"},
                "person_name": "John Doe",
                "card_type": "Visa",
                "card_last_four": "1234",
                "card_exp": "12/25",
                "bank_note": "Bank of Nowhere",
                "bank_type": "checking",
                "bank_holder_type": "personal",
                "address_1": "123 Main St",
                "address_2": "Apt 4",
                "address_city": "Orlando",
                "address_state": "FL",
                "address_zip": "32801",
                "address_country": "USA",
                "purged_at": "2023-08-10T14:00:00Z",
                "created_at": "2023-01-01T08:00:00Z",
                "updated_at": "2024-04-01T09:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(StaxPaymentMethod::class.java)
        val paymentMethod = adapter.fromJson(json)

        // Deserialization assertions
        assertNotNull(paymentMethod)
        paymentMethod!!

        assertEquals("pm123", paymentMethod.id)
        assertEquals("cust456", paymentMethod.customerId)
        assertEquals("merchant789", paymentMethod.merchantId)
        assertEquals("user321", paymentMethod.userId)
        assertEquals("Primary Card", paymentMethod.nickname)
        assertEquals(1, paymentMethod.isDefault)
        assertEquals(StaxPaymentMethodType.CARD, paymentMethod.method)
        assertEquals(mapOf("verified" to true, "issuer" to "Chase"), paymentMethod.meta)
        assertEquals("John Doe", paymentMethod.personName)
        assertEquals("Visa", paymentMethod.cardType)
        assertEquals("1234", paymentMethod.cardLastFour)
        assertEquals("12/25", paymentMethod.cardExpiry)
        assertEquals("Bank of Nowhere", paymentMethod.bankName)
        assertEquals(StaxBankAccountType.CHECKING, paymentMethod.bankType)
        assertEquals(StaxBankHolderType.PERSONAL, paymentMethod.bankHolderType)
        assertEquals("123 Main St", paymentMethod.address1)
        assertEquals("Apt 4", paymentMethod.address2)
        assertEquals("Orlando", paymentMethod.addressCity)
        assertEquals("FL", paymentMethod.addressState)
        assertEquals("32801", paymentMethod.addressZip)
        assertEquals("USA", paymentMethod.addressCountry)
        assertNotNull(paymentMethod.purgedAt)
        assertNotNull(paymentMethod.createdAt)
        assertNotNull(paymentMethod.updatedAt)

        // Serialize back to JSON
        val jsonOutput = adapter.toJson(paymentMethod)

        // Basic checks on serialized content
        assertTrue(jsonOutput.contains("\"id\":\"pm123\""))
        assertTrue(jsonOutput.contains("\"customer_id\":\"cust456\""))
        assertTrue(jsonOutput.contains("\"merchant_id\":\"merchant789\""))
        assertTrue(jsonOutput.contains("\"user_id\":\"user321\""))
        assertTrue(jsonOutput.contains("\"nickname\":\"Primary Card\""))
        assertTrue(jsonOutput.contains("\"is_default\":1"))
        assertTrue(jsonOutput.contains("\"method\":\"card\""))
        assertTrue(jsonOutput.contains("\"meta\":{\"verified\":true,\"issuer\":\"Chase\"}"))
        assertTrue(jsonOutput.contains("\"person_name\":\"John Doe\""))
        assertTrue(jsonOutput.contains("\"card_type\":\"Visa\""))
        assertTrue(jsonOutput.contains("\"card_last_four\":\"1234\""))
        assertTrue(jsonOutput.contains("\"card_exp\":\"12/25\""))
        assertTrue(jsonOutput.contains("\"bank_note\":\"Bank of Nowhere\""))
        assertTrue(jsonOutput.contains("\"bank_type\":\"checking\""))
        assertTrue(jsonOutput.contains("\"bank_holder_type\":\"personal\""))
        assertTrue(jsonOutput.contains("\"address_1\":\"123 Main St\""))
        assertTrue(jsonOutput.contains("\"address_2\":\"Apt 4\""))
        assertTrue(jsonOutput.contains("\"address_city\":\"Orlando\""))
        assertTrue(jsonOutput.contains("\"address_state\":\"FL\""))
        assertTrue(jsonOutput.contains("\"address_zip\":\"32801\""))
        assertTrue(jsonOutput.contains("\"address_country\":\"USA\""))
        assertTrue(jsonOutput.contains("\"purged_at\":\"2023-08-10T14:00:00Z\""))
        assertTrue(jsonOutput.contains("\"created_at\":\"2023-01-01T08:00:00Z\""))
        assertTrue(jsonOutput.contains("\"updated_at\":\"2024-04-01T09:00:00Z\""))
    }
}
