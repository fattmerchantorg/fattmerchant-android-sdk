package com.staxpayments.api.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class InvoiceTest {

    private val originalData = Invoice(
        id = "8858e03e-6017-41a4-9d0e-b618b7880e3f",
        total = 0.09,
        meta = null,
        status = "PAID",
        url = "https://app.staxpayments.com/#/bill/",
        reminder = null,
        schedule = null,
        customer = null,
        user = null,
        files = null,
        childTransactions = emptyList(),
        customerId = "ac097e4f-2a99-4689-8f90-29ecc2ff8455",
        merchantId = "5f5d4ddf-57a9-421c-9313-31b8d0917269",
        userId = "16212283-da27-4d1e-ab6c-5bc2cd019894",
        isMerchantPresent = true,
        sentAt = null,
        viewedAt = null,
        paidAt = "2023-01-23 00:16:11",
        scheduleId = null,
        reminderId = null,
        paymentMethodId = null,
        isWebPayment = false,
        createdAt = "2023-01-23 00:15:57",
        updatedAt = "2023-01-23 00:16:11",
        deletedAt = null,
        dueAt = null,
        isPartialPaymentEnabled = true,
        invoiceDateAt = "2023-01-23 00:15:57",
        paymentAttemptFailed = false,
        paymentAttemptMessage = "",
        balanceDue = 0.0,
        totalPaid = 0.09,
        paymentMeta = null
    )

    private val originalString =
        """{"id":"8858e03e-6017-41a4-9d0e-b618b7880e3f","total":0.09,"meta":null,"status":"PAID","url":"https://app.staxpayments.com/#/bill/","reminder":null,"schedule":null,"customer":null,"user":null,"files":null,"child_transactions":[],"customer_id":"ac097e4f-2a99-4689-8f90-29ecc2ff8455","merchant_id":"5f5d4ddf-57a9-421c-9313-31b8d0917269","user_id":"16212283-da27-4d1e-ab6c-5bc2cd019894","is_merchant_present":true,"sent_at":null,"viewed_at":null,"paid_at":"2023-01-23 00:16:11","schedule_id":null,"reminder_id":null,"payment_method_id":null,"is_webpayment":false,"created_at":"2023-01-23 00:15:57","updated_at":"2023-01-23 00:16:11","deleted_at":null,"due_at":null,"is_partial_payment_enabled":true,"invoice_date_at":"2023-01-23 00:15:57","payment_attempt_failed":false,"payment_attempt_message":"","balance_due":0.0,"total_paid":0.09,"payment_meta":null}"""

    // id is Missing in this JSON String
    private val badString =
        """{"total":0.09,"meta":null,"status":"PAID","url":"https://app.staxpayments.com/#/bill/","reminder":null,"schedule":null,"customer":null,"user":null,"files":null,"child_transactions":[],"customer_id":"ac097e4f-2a99-4689-8f90-29ecc2ff8455","merchant_id":"5f5d4ddf-57a9-421c-9313-31b8d0917269","user_id":"16212283-da27-4d1e-ab6c-5bc2cd019894","is_merchant_present":true,"sent_at":null,"viewed_at":null,"paid_at":"2023-01-23 00:16:11","schedule_id":null,"reminder_id":null,"payment_method_id":null,"is_webpayment":false,"created_at":"2023-01-23 00:15:57","updated_at":"2023-01-23 00:16:11","deleted_at":null,"due_at":null,"is_partial_payment_enabled":true,"invoice_date_at":"2023-01-23 00:15:57","payment_attempt_failed":false,"payment_attempt_message":"","balance_due":0.0,"total_paid":0.09,"payment_meta":null}"""

    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<Invoice>(originalString)
        assertEquals(originalData, restored)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<Invoice>(badString)
        assertEquals(originalData, restored)
    }
}
