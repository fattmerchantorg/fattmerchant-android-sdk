package com.fattmerchant.cpresent.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.fattmerchant.cpresent.AsyncTestRunner
import com.fattmerchant.cpresent.android.api.OmniApi
import com.fattmerchant.cpresent.omni.networking.PaginatedData
import com.fattmerchant.cpresent.android.customer.Customer
import com.fattmerchant.cpresent.android.invoice.Invoice
import com.fattmerchant.cpresent.android.payment_method.PaymentMethod
import com.fattmerchant.cpresent.android.transaction.Transaction
import com.fattmerchant.cpresent.omni.entity.Amount
import com.fattmerchant.cpresent.omni.entity.TransactionRequest
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class OmniTest: AsyncTestRunner {

    override var scope = GlobalScope
    override val mainThreadSurrogate = newSingleThreadContext("UI thread")

    fun uniqueId(): String {
        val chars =
            "abcdef012ghijkl345mnopqr678stuvwxyz91234908712345987134zcnbvaasdfy897y349rijhfskbjhvdbcuyt123514672835y4ue0projkgbnkxhjbcfvtyadwurt980sidugv8b6cvb67ihqwejrf"
        var id = ""
        for (i in 1..32) {
            id += chars[Random.nextInt(chars.lastIndex)]
            if (listOf(8, 12, 16, 20).contains(i)) {
                id += "-"
            }
        }
        return id
    }

    inner class MockApi : OmniApi {
        override suspend fun getTransactions(): PaginatedData<Transaction> {
            return PaginatedData(listOf(Transaction().apply { id = "678" }))
        }

        override suspend fun createInvoice(invoice: Invoice): Invoice {
            return invoice.apply { id = uniqueId() }
        }

        override suspend fun updateInvoice(id: String, invoice: Invoice): Invoice {
            return invoice
        }

        override suspend fun createCustomer(customer: Customer): Customer {
            return customer.apply { id = uniqueId() }
        }

        override suspend fun createTransaction(transaction: Transaction): Transaction {
            return transaction.apply { id = uniqueId() }
        }

        override suspend fun createPaymentMethod(paymentMethod: PaymentMethod): PaymentMethod {
            return paymentMethod.apply { id = uniqueId() }
        }
    }

    @Test
    @Before
    fun `can initialize with good params`() = asyncTest { completion ->
        val context = ApplicationProvider.getApplicationContext<Context>()
        val goodParams = mapOf(
            "apiKey" to "123456"
        )

        Omni.initialize(context, goodParams) {
            assert(true)
            completion()

            Assert.assertNotNull(Omni.shared())
        }
    }

    @Test(expected = Omni.InitializationError::class)
    fun `throws errors initialize with bad params fails`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Omni.initialize(context, mapOf()) { }
        Assert.assertNull(Omni.shared())
    }

    @Test
    fun `can take payment with just amount`() = asyncTest { completion ->
        val omni= Omni.shared() as Omni
        val amount = Amount(1)
        val request = TransactionRequest(amount)

        omni.takeMobileReaderTransaction(request) { result ->
            Assert.assertTrue("Mobile reader payment was unsuccessful", result.success)
            completion()
        }
    }

}
