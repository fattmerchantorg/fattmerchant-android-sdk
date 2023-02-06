package com.staxpayments.api.repository

import com.staxpayments.api.datasource.InvoiceFakeRepository
import com.staxpayments.api.requests.CreateInvoiceBody
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class InvoiceRepositoryTest {

    private var invoiceId: String? = null
    private lateinit var fakeRepository: InvoiceFakeRepository

    @Before
    fun setUp() {
        fakeRepository = InvoiceFakeRepository()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_createInvoice() = runTest(UnconfinedTestDispatcher()) {
        val body = CreateInvoiceBody(
            total = 0.09,
            url = "https://app.staxpayments.com/#/bill/",
            meta = null,
            files = null,
            customerId = "ac097e4f-2a99-4689-8f90-29ecc2ff8455",
            sendNow = true,
            isPartialPaymentEnabled = true
        )

        val response = fakeRepository.createInvoice(body)
        invoiceId = response.id

        Assert.assertNotNull(response)
        Assert.assertEquals(0.09, response.total)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_getInvoice() = runTest(UnconfinedTestDispatcher()) {
        invoiceId?.let {
            val response = fakeRepository.getInvoice(it)

            Assert.assertNotNull(response)
            Assert.assertEquals(0.09, response?.total)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_updateInvoice() = runTest(UnconfinedTestDispatcher()) {
        invoiceId?.let {
            val body = CreateInvoiceBody(
                total = 1.00,
                url = "https://app.staxpayments.com/#/bill/",
                meta = null,
                files = null,
                customerId = "ac097e4f-2a99-4689-8f90-29ecc2ff8455",
                sendNow = true,
                isPartialPaymentEnabled = false
            )

            val response = fakeRepository.updateInvoice(it, body)
            Assert.assertNotNull(response)
            Assert.assertEquals(1.00, response?.total)
        }
    }
}