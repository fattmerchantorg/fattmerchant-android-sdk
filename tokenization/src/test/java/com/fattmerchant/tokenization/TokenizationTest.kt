package com.fattmerchant.tokenization

import com.fattmerchant.models.BankAccount
import com.fattmerchant.models.CreditCard
import com.fattmerchant.models.PaymentMethod
import com.fattmerchant.tokenization.networking.FattmerchantClient
import com.fattmerchant.tokenization.networking.FattmerchantConfiguration
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch

class TokenizationTest {

    @Test
    @Throws(Exception::class)
    fun can_tokenize_credit_card() {

        // Setup a signal to wait for async call to finish
        val signal = CountDownLatch(1)

        // Setup the api client
        val config = FattmerchantConfiguration("https://apidev01.fattlabs.com", "fattwars")
        val client = FattmerchantClient(config)

        // Create the card
        val card = CreditCard.failingTestCreditCard()

        // Make the tokenization request
        client.tokenize(card).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                signal.countDown()
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                signal.countDown()
            }

        })

        signal.await()
    }

    @Test
    @Throws(Exception::class)
    fun can_tokenize_credit_card_with_listener() {

        // Setup a signal to wait for async call to finish
        val signal = CountDownLatch(1)

        // Setup the api client
        val config = FattmerchantConfiguration("https://apidev01.fattlabs.com", "fattwarser")
        val client = FattmerchantClient(config)

        // Create the card
        val card = CreditCard.failingTestCreditCard()

        client.tokenize(card, object : FattmerchantClient.TokenizationListener {
            override fun onPaymentMethodCreated(paymentMethod: PaymentMethod) {
                print(paymentMethod)
                signal.countDown()
            }

            override fun onPaymentMethodCreateError(errors: String) {
                System.out.print(errors)
            }
        })

        signal.await()
    }

    @Test
    @Throws(Exception::class)
    fun can_tokenize_bank_account() {

        // Setup a signal to wait for async call to finish
        var signal = CountDownLatch(1)

        // Setup the api client
        val config = FattmerchantConfiguration("https://apidev01.fattlabs.com", "fattwars")
        val client = FattmerchantClient(config)

        // Create the bank account
        val bankAccount = BankAccount.testBankAccount()
        bankAccount.customerId = "d08cd208-07ee-4fa6-8015-8dcb1160d4aaaaf9"

        // Make the tokenization request
        client.tokenize(bankAccount).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response?.body() == null) {
                    System.out.print(response.toString())
                } else {
                    print(response.body())
                }

                signal.countDown()
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                signal.countDown()
            }

        })

        signal.await()
    }

    private fun print(paymentMethod: PaymentMethod) = System.out.printf("Payment Method --------\n%s", paymentMethod)
}
