package com.fattmerchant

import com.fattmerchant.android.models.StaxInvoice
import com.fattmerchant.android.models.enums.StaxInvoiceStatus
import com.fattmerchant.android.serialization.UtcDateAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StaxInvoiceTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(UtcDateAdapterFactory()) // Use the custom UTC Date Adapter Factory
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun staxInvoiceJsonDeserializationAndSerializationWorksCorrectly() {
        val json = """
            {
                "id": "invoice123",
                "user_id": "user456",
                "customer_id": "customer789",
                "merchant_id": "merchant123",
                "payment_method_id": "paymentMethod1",
                "schedule_id": "schedule123",
                "balance_due": 50.75,
                "is_merchant_present": true,
                "is_webpayment": false,
                "payment_attempt_failed": true,
                "payment_attempt_message": "Payment failed",
                "status": "PAID",
                "total": 100.50,
                "total_paid": 50.75,
                "url": "http://example.com/invoice123",
                "meta": {"color": "blue", "size": "large"},
                "due_at": "2024-08-26 14:16:29",
                "sent_at": "2024-08-26 14:16:29",
                "paid_at": "2024-08-26 14:16:29",
                "viewed_at": "2024-08-26 14:16:29",
                "created_at": "2024-08-26 14:16:29",
                "updated_at": "2024-08-26 14:16:29"
            }
        """.trimIndent()

        val adapter = moshi.adapter(StaxInvoice::class.java)
        val invoice = adapter.fromJson(json)

        // Deserialization assertions (all fields)
        assertNotNull(invoice)
        invoice!!

        assertEquals("invoice123", invoice.id)
        assertEquals("user456", invoice.userId)
        assertEquals("customer789", invoice.customerId)
        assertEquals("merchant123", invoice.merchantId)
        assertEquals("paymentMethod1", invoice.paymentMethodId)
        assertEquals("schedule123", invoice.scheduleId)
        invoice.balanceDue?.let { assertEquals(50.75, it, 0.001) }
        assertTrue(invoice.isMerchantPresent!!)
        assertFalse(invoice.isWebpayment!!)
        assertTrue(invoice.hasPaymentAttemptFailed!!)
        assertEquals("Payment failed", invoice.paymentAttemptMessage)
        assertEquals(StaxInvoiceStatus.PAID, invoice.status)
        invoice.total?.let { assertEquals(100.50, it, 0.001) }
        invoice.totalPaid?.let { assertEquals(50.75, it, 0.001) }
        assertEquals("http://example.com/invoice123", invoice.url)
        assertEquals(mapOf("color" to "blue", "size" to "large"), invoice.meta)

        assertNotNull(invoice.dueAt)
        assertNotNull(invoice.sentAt)
        assertNotNull(invoice.paidAt)
        assertNotNull(invoice.viewedAt)
        assertNotNull(invoice.createdAt)
        assertNotNull(invoice.updatedAt)

        // Serialize back to JSON
        val jsonOutput = adapter.toJson(invoice)

        // Serialization checks
        assertTrue(jsonOutput.contains("\"id\":\"invoice123\""))
        assertTrue(jsonOutput.contains("\"user_id\":\"user456\""))
        assertTrue(jsonOutput.contains("\"customer_id\":\"customer789\""))
        assertTrue(jsonOutput.contains("\"merchant_id\":\"merchant123\""))
        assertTrue(jsonOutput.contains("\"payment_method_id\":\"paymentMethod1\""))
        assertTrue(jsonOutput.contains("\"schedule_id\":\"schedule123\""))
        assertTrue(jsonOutput.contains("\"balance_due\":50.75"))
        assertTrue(jsonOutput.contains("\"is_merchant_present\":true"))
        assertTrue(jsonOutput.contains("\"is_webpayment\":false"))
        assertTrue(jsonOutput.contains("\"payment_attempt_failed\":true"))
        assertTrue(jsonOutput.contains("\"payment_attempt_message\":\"Payment failed\""))
        assertTrue(jsonOutput.contains("\"status\":\"PAID\""))
        assertTrue(jsonOutput.contains("\"total\":100.5"))
        assertTrue(jsonOutput.contains("\"total_paid\":50.75"))
        assertTrue(jsonOutput.contains("\"url\":\"http://example.com/invoice123\""))
        assertTrue(jsonOutput.contains("\"meta\":{\"color\":\"blue\",\"size\":\"large\"}"))

        // Confirm UTC-formatted timestamps
        assertTrue(jsonOutput.contains("\"due_at\":\"2024-08-26 14:16:29\""))
        assertTrue(jsonOutput.contains("\"sent_at\":\"2024-08-26 14:16:29\""))
        assertTrue(jsonOutput.contains("\"paid_at\":\"2024-08-26 14:16:29\""))
        assertTrue(jsonOutput.contains("\"viewed_at\":\"2024-08-26 14:16:29\""))
        assertTrue(jsonOutput.contains("\"created_at\":\"2024-08-26 14:16:29\""))
        assertTrue(jsonOutput.contains("\"updated_at\":\"2024-08-26 14:16:29\""))
    }
}
