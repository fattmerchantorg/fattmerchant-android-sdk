package com.staxpayments.api.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class PaymentMethodTest {

    private val originalData = PaymentMethod(
        id = "6ba7babe-9906-4e7e-b1a5-f628c7badb61",
        nickname = "VISA: Steven Smith Jr. (ending in: 1111)",
        method = "card",
        meta = JsonObject(
            mapOf(
                "cardDisplay" to JsonPrimitive("484718"),
                "routingDisplay" to JsonNull,
                "accountDisplay" to JsonNull,
                "eligibleForCardUpdater" to JsonPrimitive(true),
                "storageState" to JsonPrimitive("cached"),
                "fingerprint" to JsonPrimitive("888999888777888999988")
            )
        ),
        customer = null,
        customerId = "d45ee88c-8b27-4be8-8d81-77dda1b81826",
        merchantId = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
        userId = "b58d7eee-e68d-4d12-a1f8-62f5e71382ae",
        isDefault = 1,
        binType = "DEBIT",
        personName = "Steven Smith Jr.",
        cardType = "visa",
        cardLastFour = "1111",
        cardExpiry = "042019",
        bankName = null,
        bankType = null,
        bankHolderType = null,
        address1 = null,
        address2 = null,
        addressCity = null,
        addressState = null,
        addressZip = "32944",
        addressCountry = "USA",
        purgedAt = null,
        deletedAt = "2017-05-10 19:54:09",
        createdAt = "2017-05-10 19:54:04",
        updatedAt = "2017-05-10 19:54:09",
        cardExpiryDateTime = "2019-04-30 23:59:59"
    )

    private val originalString =
        """{"id":"6ba7babe-9906-4e7e-b1a5-f628c7badb61","nickname":"VISA: Steven Smith Jr. (ending in: 1111)","method":"card","meta":{"cardDisplay":"484718","routingDisplay":null,"accountDisplay":null,"eligibleForCardUpdater":true,"storageState":"cached","fingerprint":"888999888777888999988"},"customer":null,"customer_id":"d45ee88c-8b27-4be8-8d81-77dda1b81826","merchant_id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","user_id":"b58d7eee-e68d-4d12-a1f8-62f5e71382ae","is_default":1,"bin_type":"DEBIT","person_name":"Steven Smith Jr.","card_type":"visa","card_last_four":"1111","card_exp":"042019","bank_name":null,"bank_type":null,"bank_holder_type":null,"address_1":null,"address_2":null,"address_city":null,"address_state":null,"address_zip":"32944","address_country":"USA","purged_at":null,"deleted_at":"2017-05-10 19:54:09","created_at":"2017-05-10 19:54:04","updated_at":"2017-05-10 19:54:09","card_exp_datetime":"2019-04-30 23:59:59"}"""

    // id is Missing in this JSON String
    private val badString =
        """{"nickname":"VISA: Steven Smith Jr. (ending in: 1111)","method":"card","meta":{"cardDisplay":"484718","routingDisplay":null,"accountDisplay":null,"eligibleForCardUpdater":true,"storageState":"cached","fingerprint":"888999888777888999988"},"customer":null,"customer_id":"d45ee88c-8b27-4be8-8d81-77dda1b81826","merchant_id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","user_id":"b58d7eee-e68d-4d12-a1f8-62f5e71382ae","is_default":1,"bin_type":"DEBIT","person_name":"Steven Smith Jr.","card_type":"visa","card_last_four":"1111","card_exp":"042019","bank_name":null,"bank_type":null,"bank_holder_type":null,"address_1":null,"address_2":null,"address_city":null,"address_state":null,"address_zip":"32944","address_country":"USA","purged_at":null,"deleted_at":"2017-05-10 19:54:09","created_at":"2017-05-10 19:54:04","updated_at":"2017-05-10 19:54:09","card_exp_datetime":"2019-04-30 23:59:59"}"""

    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<PaymentMethod>(originalString)
        assertEquals(originalData, restored)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<PaymentMethod>(badString)
        assertEquals(originalData, restored)
    }
}
