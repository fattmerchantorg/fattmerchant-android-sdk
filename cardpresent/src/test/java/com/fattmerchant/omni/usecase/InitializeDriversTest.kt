package com.fattmerchant.omni.usecase

//import com.fattmerchant.android.Omni
//import android.api.OmniApi
//import android.api.OmniService
//import com.fattmerchant.omni.networking.PaginatedData
//import android.customer.Customer
//import android.invoice.Invoice
//import android.payment_method.PaymentMethod
//import android.transaction.Transaction
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.*
import kotlinx.coroutines.*


private class InitializeDriversTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val scope = GlobalScope

}