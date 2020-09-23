package com.fattmerchant.tokenization.networking

import android.util.Log
import com.fattmerchant.tokenization.models.BankAccount
import com.fattmerchant.tokenization.models.CreditCard
import com.fattmerchant.tokenization.models.PaymentMethod
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.ref.WeakReference

/** Communicates with Fattmercahant */
class FattmerchantClient(override var configuration: FattmerchantConfiguration) : FattmerchantApi {

    interface TokenizationListener {

        /** Called when the FattmerchantClient successfully creates a PaymentMethod */
        fun onPaymentMethodCreated(paymentMethod: PaymentMethod)

        /** Called when the FattmerchantClient fails at creating the payment method */
        fun onPaymentMethodCreateError(errors: String)
    }

    inner class PaymentMethodCallback(
            private val weakListener: WeakReference<TokenizationListener>
    ) : Callback<PaymentMethod> {
        override fun onFailure(call: Call<PaymentMethod>, t: Throwable) {
            weakListener.get()?.onPaymentMethodCreateError(t.message ?: unknownErrorString)
        }

        override fun onResponse(call: Call<PaymentMethod>, response: Response<PaymentMethod>) {
            weakListener.get()?.let { listener ->
                if (response.isSuccessful) {
                    val paymentMethod = response.body()
                            ?: return listener.onPaymentMethodCreateError(getError(response))

                    listener.onPaymentMethodCreated(paymentMethod)
                } else {
                    listener.onPaymentMethodCreateError(getError(response))
                }
            }
        }

        private fun getError(response: Response<PaymentMethod>): String = response.errorBody()?.string()
                ?: unknownErrorString

    }


    companion object {
        const val unknownErrorString = "An unknown error occured creating the payment method"
    }

    lateinit internal var fattmerchantApi: FattmerchantApi

    init {
        configure()
    }

    private fun configure() {
        // Add headers
        val interceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")

            chain.proceed(requestBuilder.build())
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
    override fun tokenizeCreditCard(webPaymentsToken: String, card: CreditCard): Call<PaymentMethod> = fattmerchantApi.tokenizeCreditCard(webPaymentsToken, card)

    private fun tokenize(card: CreditCard): Call<PaymentMethod> = tokenizeCreditCard(configuration.webPaymentsToken, card)

    fun tokenize(card: CreditCard, listener: TokenizationListener) {
        val callback = PaymentMethodCallback(WeakReference(listener))
        tokenizeCreditCard(configuration.webPaymentsToken, card).enqueue(callback)
    }
    //endregion

    //region Bank Account Tokenization

    override fun tokenizeBankAccount(webPaymentsToken: String, card: BankAccount): Call<PaymentMethod> = fattmerchantApi.tokenizeBankAccount(webPaymentsToken, card)

    private fun tokenize(bankAccount: BankAccount): Call<PaymentMethod> = tokenizeBankAccount(configuration.webPaymentsToken, bankAccount)

    fun tokenize(bankAccount: BankAccount, listener: TokenizationListener) {
        val callback = PaymentMethodCallback(WeakReference(listener))
        tokenize(bankAccount = bankAccount).enqueue(callback)
    }
    //endregion
}
