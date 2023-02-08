package com.staxpayments.api.datasource

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.requests.InvoicePostRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class InvoiceRepositoryTest {

    private lateinit var classUnderTest: InvoiceLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    private val invoice = Invoice(
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

    private val invoicePostRequest = InvoicePostRequest(
        total = 2.09,
        url = "https://app.staxpayments.com/#/bill/",
        meta = null,
        files = null,
        customerId = "ac097e4f-2a99-4689-8f90-29ecc2ff8455",
        sendNow = true,
        isPartialPaymentEnabled = true
    )

    @Before
    fun setUp() {
        classUnderTest = InvoiceLiveRepository(
            networkClients
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given invoicePostRequest When createInvoice Then return expected invoice`() =
        runTest(UnconfinedTestDispatcher()) {
            val request = invoicePostRequest
            val expectedResult = invoice

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given invoiceId  When getInvoice Then return expected invoice`() =
        runTest(UnconfinedTestDispatcher()) {
            val expectedResult = invoice
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given invoicePostRequest When updateInvoice Then return expected invoice`() =
        runTest(UnconfinedTestDispatcher()) {
            val request = invoicePostRequest
            val expectedResult = invoice
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