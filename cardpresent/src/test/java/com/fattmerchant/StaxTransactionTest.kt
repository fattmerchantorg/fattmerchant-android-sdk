package com.fattmerchant

import com.fattmerchant.android.models.StaxTransaction
import com.fattmerchant.android.models.enums.Currency
import com.fattmerchant.android.models.enums.TransactionType
import com.fattmerchant.android.serialization.UtcDateAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StaxTransactionTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(UtcDateAdapterFactory())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun staxTransactionJsonDeserializationAndSerializationWorksCorrectly() {
        val json = """
            {
                "id": "txn_001",
                "invoice_id": "inv_001",
                "reference_id": "ref_001",
                "recurring_transaction_id": "rec_001",
                "auth_id": "auth_001",
                "type": "charge",
                "source": "web",
                "source_ip": "127.0.0.1",
                "is_merchant_present": true,
                "merchant_id": "merch_001",
                "user_id": "user_001",
                "customer_id": "cust_001",
                "payment_method_id": "pm_001",
                "is_manual": false,
                "spreedly_token": "token_abc",
                "spreedly_response": "success",
                "success": true,
                "message": "Transaction approved",
                "meta": "test metadata",
                "total": 99.99,
                "method": "card",
                "pre_auth": false,
                "is_captured": true,
                "last_four": "4242",
                "interchange_code": "IC123",
                "interchange_fee": 1.25,
                "batch_id": "batch_001",
                "batched_at": "2024-08-26 14:16:29",
                "emv_response": "emv_ok",
                "avs_response": "Y",
                "cvv_response": "M",
                "pos_entry": "chip",
                "pos_salesperson": "John Doe",
                "receipt_email_at": "2024-08-26 14:17:00",
                "receipt_sms_at": "2024-08-26 14:18:00",
                "settled_at": "2024-08-26 14:19:00",
                "created_at": "2024-08-26 14:20:00",
                "updated_at": "2024-08-26 14:21:00",
                "gateway_id": "gw_001",
                "issuer_auth_code": "123456",
                "channel": "online",
                "currency": "USD"
            }
        """.trimIndent()

        val adapter = moshi.adapter(StaxTransaction::class.java)
        val transaction = adapter.fromJson(json)

        // Deserialization assertions
        assertNotNull(transaction)
        transaction!!

        assertEquals("txn_001", transaction.id)
        assertEquals("inv_001", transaction.invoiceId)
        assertEquals("ref_001", transaction.referenceId)
        assertEquals("rec_001", transaction.recurringTransactionId)
        assertEquals("auth_001", transaction.authId)
        assertEquals(TransactionType.CHARGE, transaction.type)
        assertEquals("web", transaction.source)
        assertEquals("127.0.0.1", transaction.sourceIp)
        assertTrue(transaction.isMerchantPresent ?: false)
        assertEquals("merch_001", transaction.merchantId)
        assertEquals("user_001", transaction.userId)
        assertEquals("cust_001", transaction.customerId)
        assertEquals("pm_001", transaction.paymentMethodId)
        assertFalse(transaction.isManual ?: true)
        assertEquals("token_abc", transaction.spreedlyToken)
        assertEquals("success", transaction.spreedlyResponse)
        assertTrue(transaction.success ?: false)
        assertEquals("Transaction approved", transaction.message)
        assertEquals("test metadata", transaction.meta)
        assertEquals(99.99, transaction.total ?: 0.0, 0.001)
        assertEquals("card", transaction.method)
        assertFalse(transaction.preAuth ?: true)
        assertTrue(transaction.isCaptured ?: false)
        assertEquals("4242", transaction.lastFour)
        assertEquals("IC123", transaction.interchangeCode)
        assertEquals(1.25, transaction.interchangeFee ?: 0.0, 0.001)
        assertEquals("batch_001", transaction.batchId)
        assertNotNull(transaction.batchedAt)
        assertEquals("emv_ok", transaction.emvResponse)
        assertEquals("Y", transaction.avsResponse)
        assertEquals("M", transaction.cvvResponse)
        assertEquals("chip", transaction.posEntry)
        assertEquals("John Doe", transaction.posSalesperson)
        assertNotNull(transaction.receiptEmailAt)
        assertNotNull(transaction.receiptSmsAt)
        assertNotNull(transaction.settledAt)
        assertNotNull(transaction.createdAt)
        assertNotNull(transaction.updatedAt)
        assertEquals("gw_001", transaction.gatewayId)
        assertEquals("123456", transaction.issuerAuthCode)
        assertEquals("online", transaction.channel)
        assertEquals(Currency.USD, transaction.currency)

        // Serialization
        val jsonOutput = adapter.toJson(transaction)

        assertTrue(jsonOutput.contains("\"id\":\"txn_001\""))
        assertTrue(jsonOutput.contains("\"invoice_id\":\"inv_001\""))
        assertTrue(jsonOutput.contains("\"type\":\"charge\""))
        assertTrue(jsonOutput.contains("\"total\":99.99"))
        assertTrue(jsonOutput.contains("\"currency\":\"USD\""))
        assertTrue(jsonOutput.contains("\"created_at\":\"2024-08-26 14:20:00\""))
    }
}
