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
class UserTest {

    private val originalData = User(
        id = "82bec9c3-0259-45d4-b96b-6e1fe6a57908",
        name = "api-key",
        email = "osagie.omon@fattmerchant.com",
        createdAt = "2022-11-16 19:04:33",
        updatedAt = "2022-11-16 19:04:33",
        emailVerificationSentAt = null,
        emailVerifiedAt = null,
        isDefault = false,
        isApiKey = true,
        deletedAt = null,
        systemAdmin = false,
        teamRole = "admin",
        teamAdmin = true,
        teamEnabled = true,
        mfaEnabled = false,
        merchantOptions = emptyList(),
        gravatar = "www.gravatar.com/avatar/d41d8cd98f00b204e9800998ecf8427e",
        acknowledgments = null,
        options = null,
        brand = "fattmerchant"
    )

    private val originalString =
        """{"id":"82bec9c3-0259-45d4-b96b-6e1fe6a57908","name":"api-key","email":"osagie.omon@fattmerchant.com","created_at":"2022-11-16 19:04:33","updated_at":"2022-11-16 19:04:33","email_verification_sent_at":null,"email_verified_at":null,"is_default":false,"is_api_key":true,"deleted_at":null,"system_admin":false,"team_role":"admin","team_admin":true,"team_enabled":true,"mfa_enabled":false,"merchant_options":[],"gravatar":"www.gravatar.com/avatar/d41d8cd98f00b204e9800998ecf8427e","acknowledgments":null,"options":null,"brand":"fattmerchant"}"""

    // id is Missing in this JSON String
    private val badString =
        """{"name":"api-key","email":"osagie.omon@fattmerchant.com","created_at":"2022-11-16 19:04:33","updated_at":"2022-11-16 19:04:33","email_verification_sent_at":null,"email_verified_at":null,"is_default":false,"is_api_key":true,"deleted_at":null,"system_admin":false,"team_role":"admin","team_admin":true,"team_enabled":true,"mfa_enabled":false,"merchant_options":[],"gravatar":"www.gravatar.com/avatar/d41d8cd98f00b204e9800998ecf8427e","acknowledgments":null,"options":null,"brand":"fattmerchant"}"""

    @Test
    fun test_stringForm() {
        val str = Json.encodeToString(originalData)
        assertEquals(originalString, str)
    }

    @Test
    fun test_serializeBack() {
        val restored = Json.decodeFromString<User>(originalString)
        assertEquals(originalData, restored)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun test_FailedSerializeBack() {
        val restored = Json.decodeFromString<User>(badString)
        assertEquals(originalData, restored)
    }
}


