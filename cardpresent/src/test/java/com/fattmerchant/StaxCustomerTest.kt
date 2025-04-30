package com.fattmerchant

import com.fattmerchant.android.models.StaxCustomer
import com.fattmerchant.android.serialization.UtcDateAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StaxCustomerTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(UtcDateAdapterFactory()) // Use the custom UTC Date Adapter Factory
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun staxCustomerJsonDeserializationAndSerializationWorksCorrectly() {
        val json = """
            {
                "id": "cust001",
                "firstname": "Jane",
                "lastname": "Doe",
                "company": "Doe Industries",
                "email": "jane.doe@example.com",
                "cc_emails": ["accounting@example.com", "backup@example.com"],
                "cc_sms": ["1234567890"],
                "phone": "9876543210",
                "address_1": "123 Main St",
                "address_2": "Suite 4B",
                "address_city": "Orlando",
                "address_state": "FL",
                "address_zip": "32801",
                "address_country": "USA",
                "notes": "Preferred customer",
                "reference": "REF123",
                "allow_invoice_credit_card_payments": true,
                "has_address": true,
                "parent_merge": null,
                "child_merges": ["child1", "child2"],
                "created_at": "2023-05-01T10:00:00Z",
                "updated_at": "2024-04-01T08:30:00Z"
            }
        """.trimIndent()

        // Deserialization assertions (all fields)
        val adapter = moshi.adapter(StaxCustomer::class.java)
        val customer = adapter.fromJson(json)
        assertNotNull(customer)

        assertEquals("cust001", customer?.id)
        assertEquals("Jane", customer?.firstname)
        assertEquals("Doe", customer?.lastname)
        assertEquals("Doe Industries", customer?.company)
        assertEquals("jane.doe@example.com", customer?.email)
        assertEquals("9876543210", customer?.phone)
        assertEquals("123 Main St", customer?.address1)
        assertEquals("Suite 4B", customer?.address2)
        assertEquals("Orlando", customer?.addressCity)
        assertEquals("FL", customer?.addressState)
        assertEquals("32801", customer?.addressZip)
        assertEquals("USA", customer?.addressCountry)
        assertEquals("Preferred customer", customer?.notes)
        assertEquals("REF123", customer?.reference)
        assertEquals(true, customer?.allowInvoiceCreditCardPayments)
        assertEquals(true, customer?.hasAddress)
        assertNull(customer?.parentMerge)
        assertEquals(listOf("accounting@example.com", "backup@example.com"), customer?.ccEmails)
        assertEquals(listOf("1234567890"), customer?.ccSms)
        assertEquals(listOf("child1", "child2"), customer?.childMerges)

        // Serialize back to JSON
        val jsonOutput = adapter.toJson(customer)

        // Serialization checks
        assertTrue(jsonOutput.contains("\"id\":\"cust001\""))
        assertTrue(jsonOutput.contains("\"firstname\":\"Jane\""))
        assertTrue(jsonOutput.contains("\"lastname\":\"Doe\""))
        assertTrue(jsonOutput.contains("\"company\":\"Doe Industries\""))
        assertTrue(jsonOutput.contains("\"email\":\"jane.doe@example.com\""))
        assertTrue(jsonOutput.contains("\"phone\":\"9876543210\""))
        assertTrue(jsonOutput.contains("\"address_1\":\"123 Main St\""))
        assertTrue(jsonOutput.contains("\"address_2\":\"Suite 4B\""))
        assertTrue(jsonOutput.contains("\"address_city\":\"Orlando\""))
        assertTrue(jsonOutput.contains("\"address_state\":\"FL\""))
        assertTrue(jsonOutput.contains("\"address_zip\":\"32801\""))
        assertTrue(jsonOutput.contains("\"address_country\":\"USA\""))
        assertTrue(jsonOutput.contains("\"notes\":\"Preferred customer\""))
        assertTrue(jsonOutput.contains("\"reference\":\"REF123\""))
        assertTrue(jsonOutput.contains("\"allow_invoice_credit_card_payments\":true"))
        assertTrue(jsonOutput.contains("\"has_address\":true"))
        assertTrue(jsonOutput.contains("\"child_merges\":[\"child1\",\"child2\"]"))

        // Confirm UTC-formatted timestamps
        assertTrue(jsonOutput.contains("\"created_at\":\"2023-05-01T10:00:00Z\""))
        assertTrue(jsonOutput.contains("\"updated_at\":\"2024-04-01T08:30:00Z\""))
    }
}
