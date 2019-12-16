package omni.usecase

//import android.Omni
//import android.api.OmniApi
//import android.api.OmniService
//import omni.networking.PaginatedData
//import android.customer.Customer
//import android.invoice.Invoice
//import android.payment_method.PaymentMethod
//import android.transaction.Transaction
import omni.data.*
import omni.data.models.Transaction
import omni.data.repository.*
import kotlinx.coroutines.*


class InitializeDriversTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val scope = GlobalScope

    val mockMobileReaderDriverRepository = object: MobileReaderDriverRepository {

        override suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver? {
            return null
        }

        override suspend fun getDriverFor(mobileReader: MobileReader): MobileReaderDriver? {
            return null
        }

        override suspend fun getDrivers(): List<MobileReaderDriver> {
            return listOf()
        }
    }


}