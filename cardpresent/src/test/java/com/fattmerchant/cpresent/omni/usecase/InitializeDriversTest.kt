package com.fattmerchant.cpresent.omni.usecase

//import com.fattmerchant.cpresent.android.Omni
//import com.fattmerchant.cpresent.android.api.OmniApi
//import com.fattmerchant.cpresent.android.api.OmniService
//import com.fattmerchant.cpresent.omni.networking.PaginatedData
//import com.fattmerchant.cpresent.android.customer.Customer
//import com.fattmerchant.cpresent.android.invoice.Invoice
//import com.fattmerchant.cpresent.android.payment_method.PaymentMethod
//import com.fattmerchant.cpresent.android.transaction.Transaction
import com.fattmerchant.cpresent.android.api.OmniApi
import com.fattmerchant.cpresent.omni.entity.*
import com.fattmerchant.cpresent.omni.entity.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random


class InitializeDriversTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val scope = GlobalScope

    val mockMobileReaderDriverRepository = object: MobileReaderDriverRepository {
        override suspend fun getDrivers(): List<MobileReaderDriver> {
            return listOf()
        }
    }


}