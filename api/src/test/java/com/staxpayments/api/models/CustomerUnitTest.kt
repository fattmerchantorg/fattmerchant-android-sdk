package com.staxpayments.api.models

import com.staxpayments.api.models.Customer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class CustomerUnitTest {

    private val originalData = Customer(
        id = "680afd1b-713f-46c1-af46-1a56b8b5f8a6",
        firstName = "A",
        lastName = "GIFTFORYOU",
        company = "",
        email = "ted88@gmail.com",
        CCemails = emptyList(),
        CCsms = null,
        phone = "",
        address1 = "",
        address2 = "",
        addressCity = "",
        addressState = "",
        addressZip = "",
        addressCountry = "",
        notes = "",
        reference = "",
        options = null,
        createdAt = "2020-10-07 15:50:39",
        updatedAt = "2022-01-31 21:59:09",
        deletedAt = null,
        doesAllowInvoiceCreditCardPayments = true,
        gravatar = "//www.gravatar.com/avatar/f3f5de60b600a14d62657be794b7b23d",
        hasAddress = true,
        missingAddressComponents = emptyList()
    )

    private val originalString = """{"id":"680afd1b-713f-46c1-af46-1a56b8b5f8a6","firstname":"A","lastname":"GIFTFORYOU","address_1":"","address_2":"","address_city":"","address_country":"","address_state":"","address_zip":"","cc_emails":[],"cc_sms":null,"has_address":true,"missing_address_components":[],"allow_invoice_credit_card_payments":true,"updated_at":"2022-01-31 21:59:09","created_at":"2020-10-07 15:50:39","deleted_at":null,"email":"ted88@gmail.com","gravatar":"//www.gravatar.com/avatar/f3f5de60b600a14d62657be794b7b23d","company":"","notes":"","options":null,"phone":"","reference":""}"""

    // id is Missing in this JSON String
    private val badString = """{"id":"680afd1b-713f-46c1-af46-1a56b8b5f8a6","company":"","notes":"","options":null,"phone":"","reference":""}"""
    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<Customer>(originalString)
        assertEquals(originalData, restored)
    }

    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<Customer>(badString)
        assertEquals(originalData, restored)
    }
}
