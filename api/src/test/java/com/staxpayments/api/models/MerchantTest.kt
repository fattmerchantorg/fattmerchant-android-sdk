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
class MerchantTest {

    private val originalData = Merchant(
        id = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
        mid = "520000294774",
        status = "ACTIVE",
        subdomain = "demo",
        plan = Plan(
            id = "d619a0cc-b7e1-11e6-a0aa-08002777c33d",
            merchantId = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
            userId = null,
            name = "premium",
            createdAt = "2016-12-01 16:18:46",
            updatedAt = "2016-12-01 16:18:46"
        ),
        options = null,
        processor = "Vantiv",
        branding = null,
        currency = listOf("USD"),
        companyName = "Here",
        contactName = "Stax",
        contactEmail = "contact@example.com",
        contactPhone = "8555503288",
        address1 = "25 Wall Street",
        address2 = "Suite 1",
        addressCity = "Orlando",
        addressState = "FL",
        addressZip = "32801",
        hostedPaymentsToken = "okay",
        gatewayType = "test",
        productType = "Terminal",
        welcomeEmailSentAt = null,
        createdAt = "2016-12-01 16:18:46",
        updatedAt = "2017-04-21 20:15:12",
        deletedAt = null,
        gatewayName = null,
        allowAch = true
    )

    private val originalString =
        """{"id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","mid":"520000294774","status":"ACTIVE","subdomain":"demo","plan":{"id":"d619a0cc-b7e1-11e6-a0aa-08002777c33d","name":"premium","merchant_id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","user_id":null,"created_at":"2016-12-01 16:18:46","updated_at":"2016-12-01 16:18:46"},"options":null,"processor":"Vantiv","branding":null,"currency":["USD"],"company_name":"Here","contact_name":"Stax","contact_email":"contact@example.com","contact_phone":"8555503288","address_1":"25 Wall Street","address_2":"Suite 1","address_city":"Orlando","address_state":"FL","address_zip":"32801","hosted_payments_token":"okay","gateway_type":"test","product_type":"Terminal","welcome_email_sent_at":null,"created_at":"2016-12-01 16:18:46","updated_at":"2017-04-21 20:15:12","deleted_at":null,"gateway_name":null,"allow_ach":true}"""

    // id is Missing in this JSON String
    private val badString =
        """{"mid":"520000294774","status":"ACTIVE","subdomain":"demo","plan":{"id":"d619a0cc-b7e1-11e6-a0aa-08002777c33d","name":"premium","merchant_id":"dd36b936-1eb7-4ece-bebc-b514c6a36ebd","user_id":null,"created_at":"2016-12-01 16:18:46","updated_at":"2016-12-01 16:18:46"},"options":null,"processor":"Vantiv","branding":null,"currency":["USD"],"company_name":"Here","contact_name":"Stax","contact_email":"contact@example.com","contact_phone":"8555503288","address_1":"25 Wall Street","address_2":"Suite 1","address_city":"Orlando","address_state":"FL","address_zip":"32801","hosted_payments_token":"okay","gateway_type":"test","product_type":"Terminal","welcome_email_sent_at":null,"created_at":"2016-12-01 16:18:46","updated_at":"2017-04-21 20:15:12","deleted_at":null,"gateway_name":null,"allow_ach":true}"""


    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<Merchant>(originalString)
        assertEquals(originalData, restored)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<Merchant>(badString)
        assertEquals(originalData, restored)
    }
}
