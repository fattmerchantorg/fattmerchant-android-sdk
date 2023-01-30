package com.staxpayments.api.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionTest {

    private val originalData = Transaction(
        id = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        userId = "b58d7eee-e68d-4d12-a1f8-62f5e71382ae",
        referenceId = "6f49149d-69a1-48e6-bec5-d63eb5816118",
        invoiceId = "",
        recurringTransactionId = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        source = null,
        merchantId = "6f49149d-69a1-48e6-bec5-d63eb5816118",
        customerId = "",
        paymentMethodId = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        isManual = null,
        success = false,
        message = null,
        meta = null,
        createdAt = "10-10-2022",
        isRefundable = false,
        isVoidable = false,
        isVoided = false,
        lastFour = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        paymentMethod = null,
        preAuth = false,
        receiptEmailAt = null,
        type = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        receiptSmsAt = null,
        user = null,
        scheduleId = null,
        totalRefunded = null,
        total = 3,
        files = null,
        method = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        currency = null,
        customer = null,
        childTransactions = null,
        updatedAt = "10-10-2022",
    )

    private val originalString =
        """{"child_transactions":null,"created_at":"10-10-2022","customer_id":"","invoice_id":"","is_manual":null,"is_refundable":false,"is_voidable":false,"is_voided":false,"last_four":"79256d84-3930-4e4b-bc71-9c6587cfc65c","merchant_id":"6f49149d-69a1-48e6-bec5-d63eb5816118","payment_method":null,"payment_method_id":"79256d84-3930-4e4b-bc71-9c6587cfc65c","pre_auth":false,"receipt_email_at":null,"receipt_sms_at":null,"recurring_transaction_id":"79256d84-3930-4e4b-bc71-9c6587cfc65c","reference_id":"6f49149d-69a1-48e6-bec5-d63eb5816118","schedule_id":null,"total_refunded":null,"updated_at":"10-10-2022","user_id":"b58d7eee-e68d-4d12-a1f8-62f5e71382ae","user":null,"type":"79256d84-3930-4e4b-bc71-9c6587cfc65c","source":null,"success":false,"total":3,"message":null,"meta":null,"method":"79256d84-3930-4e4b-bc71-9c6587cfc65c","files":null,"id":"79256d84-3930-4e4b-bc71-9c6587cfc65c","currency":null,"customer":null}"""

    private val badString =
        """{"child_transactions":null,"created_at":"10-10-2022","customer_id":"","invoice_id":"","is_manual":null,"is_refundable":false,"is_voidable":false,"is_voided":false,"last_four":"79256d84-3930-4e4b-bc71-9c6587cfc65c","merchant_id":"6f49149d-69a1-48e6-bec5-d63eb5816118","payment_method":null,"payment_method_id":"79256d84-3930-4e4b-bc71-9c6587cfc65c","pre_auth":false,"receipt_email_at":null,"receipt_sms_at":null,"recurring_transaction_id":"79256d84-3930-4e4b-bc71-9c6587cfc65c","reference_id":"6f49149d-69a1-48e6-bec5-d63eb5816118","schedule_id":null,"total_refunded":null,"updated_at":"10-10-2022","user_id":"b58d7eee-e68d-4d12-a1f8-62f5e71382ae","user":null,"type":"79256d84-3930-4e4b-bc71-9c6587cfc65c","source":null,"success":false,"total":3,"message":null}"""

    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<Transaction>(originalString)
        assertEquals(originalData, restored)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<Transaction>(badString)
        assertEquals(originalData, restored)
    }
}
