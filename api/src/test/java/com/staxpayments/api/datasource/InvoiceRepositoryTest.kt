package com.staxpayments.api.datasource

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.network.NetworkClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class InvoiceRepositoryTest {

    private lateinit var classUnderTest: InvoiceLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    private val invoiceResponse = Invoice(
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
        paymentMeta = null,
        sendNow = null
    )

    private val invoiceRequest = Invoice(
        total = 2.09,
        url = "https://app.staxpayments.com/#/bill/",
        sendNow = true,
        customerId = "ac097e4f-2a99-4689-8f90-29ecc2ff8455",
        isPartialPaymentEnabled = true
    )

    @Before
    fun setUp() {
        classUnderTest = InvoiceLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given invoiceRequest When createInvoice Then return expected invoice`() =
        runTest {
            val request = invoiceRequest
            val expectedResult = invoiceResponse

            //given
            given(
                networkClients.post(
                    path = "invoice",
                    request = request,
                    responseType = Invoice.serializer()
                )
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.createInvoice(request)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given invoiceId When getInvoice Then return expected invoice`() =
        runTest {
            val expectedResult = invoiceResponse
            val invoiceId = "8858e03e-6017-41a4-9d0e-b618b7880e3f"

            //given
            given(
                networkClients.get("invoice/$invoiceId", responseType = Invoice.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getInvoice(invoiceId = invoiceId)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given invoiceRequest When updateInvoice Then return expected invoice`() =
        runTest {
            val request = invoiceRequest
            val expectedResult = invoiceResponse
            val invoiceId = "8858e03e-6017-41a4-9d0e-b618b7880e3f"

            //given
            given(
                networkClients.put(
                    path = "invoice/$invoiceId",
                    request = request,
                    responseType = Invoice.serializer()
                )
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.updateInvoice(invoiceId, request)

            // Then
            assertEquals(expectedResult, actualCommand)
        }
}
