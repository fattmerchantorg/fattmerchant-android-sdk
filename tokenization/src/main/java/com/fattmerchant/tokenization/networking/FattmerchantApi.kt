package com.fattmerchant.tokenization.networking

import com.fattmerchant.tokenization.models.BankAccount
import com.fattmerchant.tokenization.models.CreditCard
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/** Defines the Fattmerchant Tokenization API */
interface FattmerchantApi {

    /** An object used to configure the FattmerchantApi */
    var configuration: FattmerchantConfiguration

    @POST("/webpayment/{webPaymentsToken}/tokenize")
    fun tokenizeCreditCard(@Path("webPaymentsToken") webPaymentsToken: String, @Body card: CreditCard)
    : Call<ResponseBody>

    @POST("/webpayment/{webPaymentsToken}/tokenize")
    fun tokenizeBankAccount(@Path("webPaymentsToken") webPaymentsToken: String, @Body card: BankAccount)
    : Call<ResponseBody>

}
