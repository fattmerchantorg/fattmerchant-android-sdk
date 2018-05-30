package com.fattmerchant.tokenization.networking

import com.fattmerchant.models.BankAccount
import com.fattmerchant.models.CreditCard
import com.fattmerchant.models.PaymentMethod
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** Communicates with Fattmercahant */
class FattmerchantClient(override var configuration: FattmerchantConfiguration) : FattmerchantApi {

    interface TokenizationListener {

        /** Called when the FattmerchantClient successfully creates a PaymentMethod */
        fun onPaymentMethodCreated(paymentMethod: PaymentMethod)

        /** Called when the FattmerchantClient fails at creating the payment method */
        fun onPaymentMethodCreateError(errors: String)
    }

    companion object {
        const val unknownErrorString = "An unknown error occured creating the payment method"
    }

    lateinit private var fattmerchantApi: FattmerchantApi

    init { configure() }

    private fun configure() {
        // Add headers
        val interceptor = Interceptor { chain ->
            val requestBuilder = chain.request()?.newBuilder()
                    ?.addHeader("Accept", "application/json")
                    ?.addHeader("Content-Type", "application/json")

            chain.proceed(requestBuilder!!.build())
        }

        val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(configuration.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build()

        fattmerchantApi = retrofit.create(FattmerchantApi::class.java)
    }

    //region Credit Card Tokenization
    override fun tokenizeCreditCard(webPaymentsToken: String, card: CreditCard): Call<ResponseBody> = fattmerchantApi.tokenizeCreditCard(webPaymentsToken, card)

    fun tokenize(card: CreditCard): Call<ResponseBody> = tokenizeCreditCard(configuration.webPaymentsToken, card)

    fun tokenize(card: CreditCard, listener: TokenizationListener) {
        tokenize(card).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response?.body() != null && response.body() is PaymentMethod) {
                    listener.onPaymentMethodCreated(response.body() as PaymentMethod)
                } else {
                    var errorString = response?.errorBody()?.string()
                    listener.onPaymentMethodCreateError(errorString ?: unknownErrorString)
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                listener.onPaymentMethodCreateError(t?.message ?: unknownErrorString)
            }

        })
    }
    //endregion

    //region Bank Account Tokenization

    override fun tokenizeBankAccount(webPaymentsToken: String, card: BankAccount): Call<ResponseBody> = fattmerchantApi.tokenizeBankAccount(webPaymentsToken, card)

    fun tokenize(bankAccount: BankAccount) : Call<ResponseBody> = tokenizeBankAccount(configuration.webPaymentsToken, bankAccount)

    fun tokenize(bankAccount: BankAccount, listener: TokenizationListener) {
        tokenize(bankAccount = bankAccount).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response?.body() != null && response.body() is PaymentMethod) {
                    listener.onPaymentMethodCreated(response.body() as PaymentMethod)
                } else {
                    var errorString = response?.errorBody()?.string()
                    listener.onPaymentMethodCreateError(errorString ?: unknownErrorString)
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                listener.onPaymentMethodCreateError(t?.message ?: unknownErrorString)
            }

        })
    }
    //endregion
}
