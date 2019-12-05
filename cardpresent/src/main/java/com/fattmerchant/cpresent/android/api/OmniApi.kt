package com.fattmerchant.cpresent.android.api

import com.fattmerchant.cpresent.android.transaction.Transaction
import com.fattmerchant.cpresent.android.customer.Customer
import com.fattmerchant.cpresent.android.invoice.Invoice
import com.fattmerchant.cpresent.android.payment_method.PaymentMethod
import com.fattmerchant.cpresent.omni.networking.PaginatedData

import retrofit2.http.*

/** Defines the Fattmerchant API */
interface OmniApi {

    @POST("/invoice")
    suspend fun createInvoice(@Body invoice: Invoice): Invoice

    @PUT("/invoice/{id}")
    suspend fun updateInvoice(@Path("id") id: String, @Body invoice: Invoice): Invoice

    @POST("/customer")
    suspend fun createCustomer(@Body customer: Customer): Customer

    @POST("/transaction")
    suspend fun createTransaction(@Body transaction: Transaction): Transaction

    @GET("/transaction")
    suspend fun getTransactions(): PaginatedData<Transaction>

    @POST("/payment-method")
    suspend fun createPaymentMethod(@Body paymentMethod: PaymentMethod): PaymentMethod
}
