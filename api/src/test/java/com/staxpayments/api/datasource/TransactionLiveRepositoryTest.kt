package com.staxpayments.api.datasource

import com.staxpayments.api.models.Transaction
import com.staxpayments.api.network.NetworkClient
import kotlinx.coroutines.runBlocking
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

@RunWith(MockitoJUnitRunner::class)
class TransactionLiveRepositoryTest {

    private val transaction = Transaction(
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

    private lateinit var classUnderTest: TransactionLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    @Before
    fun setup() {
        classUnderTest = TransactionLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given transactionId  When getTransactionById Then return expected Transaction`() =
        runBlocking {

            val expectedResult = transaction
            val id = "123"

            // given
            given(
                networkClients.get("transaction/$id", responseType = Transaction.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getTransactionById(id = id)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given transactionRequest  When charge Then return expected Transaction`() =
        runBlocking {

            val request = transaction
            val expectedResult = transaction

            // given
            given(
                networkClients.post("charge", request = request, responseType = Transaction.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.charge(request)

            // Then
            assertEquals(expectedResult, actualCommand)
        }
}
