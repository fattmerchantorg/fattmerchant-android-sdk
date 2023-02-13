package com.staxpayments.api.datasource

import com.staxpayments.api.models.Merchant
import com.staxpayments.api.network.NetworkClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MerchantRepositoryTest {

    private lateinit var classUnderTest: MerchantLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

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

    @Before
    fun setUp() {
        classUnderTest = MerchantLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given merchantId When getMerchant Then return expected merchant`() =
        runTest {
            val merchantId = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd"
            val expectedResult = merchant

            //given
            BDDMockito.given(
                networkClients.get("merchant/$merchantId", responseType = Merchant.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getMerchant(merchantId)

            // Then
            Assert.assertEquals(expectedResult, actualCommand)
        }
}
