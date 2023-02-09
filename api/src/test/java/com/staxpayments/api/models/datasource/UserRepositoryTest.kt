package com.staxpayments.api.models.datasource

import com.staxpayments.api.datasource.UserLiveRepository
import com.staxpayments.api.models.Merchant
import com.staxpayments.api.models.User
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.responses.UserResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryTest {

    private lateinit var classUnderTest: UserLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    private val user = User(
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

    private val merchant = Merchant(
        id = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
        mid = "520000294774",
        status = "ACTIVE",
        subdomain = "demo",
        plan = JsonObject(
            mapOf(
                "id" to JsonPrimitive("d619a0cc-b7e1-11e6-a0aa-08002777c33d"),
                "name" to JsonPrimitive("premium"),
                "merchant_id" to JsonPrimitive("dd36b936-1eb7-4ece-bebc-b514c6a36ebd"),
                "user_id" to JsonNull,
                "created_at" to JsonPrimitive("2016-12-01 16:18:46"),
                "updated_at" to JsonPrimitive("2016-12-01 16:18:46")
            )
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
        doesAllowAch = true
    )

    private val userResponse = UserResponse(
        user = user,
        merchant = merchant
    )

    @Before
    fun setUp() {
        classUnderTest = UserLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given authenticated user When getUser Then return expected userResponse`() =
        runTest {
            val expectedResult = userResponse

            //given
            given(
                networkClients.get("self", responseType = UserResponse.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getUser()

            // Then
            assertEquals(expectedResult, actualCommand)
        }
}
