package com.staxpayments.api.datasource

import com.staxpayments.api.models.PaymentMethod
import com.staxpayments.api.network.NetworkClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class PaymentMethodRepositoryTest {

    private lateinit var classUnderTest: PaymentMethodLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    private val paymentMethod = PaymentMethod(
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
        message = null,
        paymentMethod = null,
        paymentMethodId = null,
        receiptEmailAt = null,
        receiptSmsAt = null,
        recurringTransactionId = null,
        referenceId = null,
        scheduleId = null,
        customerId = "d45ee88c-8b27-4be8-8d81-77dda1b81826",
        merchantId = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
        userId = "b58d7eee-e68d-4d12-a1f8-62f5e71382ae",
        isDefault = 1,
        binType = "DEBIT",
        personName = "Steven Smith Jr.",
        cardType = "visa",
        cardLastFour = "1111",
        cardExpiry = "042019",
        user = null,
        type = null,
        transactions = null,
        totalRefunded = null,
        total = null,
        success = null,
        source = null,
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
        cardExpiryDateTime = "2019-04-30 23:59:59",
        currency = null,
        files = null,
        invoiceId = null,
        isManual = null,
        isPreAuth = null,
        isRefundable = null,
        isVoidable = null,
        customer = null,
        isVoided = false,
        lastFour = null
    )

    @Before
    fun setUp() {
        classUnderTest = PaymentMethodLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given paymentMethodId When getPaymentMethod Then return expected paymentMethod`() =
        runTest {
            val paymentMethodId = "6ba7babe-9906-4e7e-b1a5-f628c7badb61"
            val expectedResult = paymentMethod

            //given
            BDDMockito.given(
                networkClients.get("payment-method/$paymentMethodId", responseType = PaymentMethod.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getPaymentMethod(paymentMethodId)

            // Then
            assertEquals(expectedResult, actualCommand)
        }
}
