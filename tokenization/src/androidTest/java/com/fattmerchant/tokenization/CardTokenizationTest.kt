package com.fattmerchant.tokenization

import androidx.test.runner.AndroidJUnit4
import com.fattmerchant.tokenization.models.BankAccount
import com.fattmerchant.tokenization.models.CreditCard
import com.fattmerchant.tokenization.models.PaymentMethod
import com.fattmerchant.tokenization.networking.FattmerchantClient
import com.fattmerchant.tokenization.networking.FattmerchantConfiguration
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CardTokenizationTest {

    lateinit var client: FattmerchantClient

    @Before
    fun setup() {
        val config = FattmerchantConfiguration("https://apidev.fattlabs.com", "d5bdb73f-e49b-448e-927e-952b3347c9be")
        client = FattmerchantClient(config)
    }

    @Test
    fun can_tokenize_credit_card() {
        val signal = CountDownLatch(1)
        val card = CreditCard.testCreditCard()

        // Make the tokenization request
        client.tokenize(card, object : FattmerchantClient.TokenizationListener {
            override fun onPaymentMethodCreated(paymentMethod: PaymentMethod) {
                assertEquals(paymentMethod.personName, card.personName)
                assertEquals(paymentMethod.method, "card")
                assertNotNull(paymentMethod.id)
                signal.countDown()
            }

            override fun onPaymentMethodCreateError(errors: String) {
                fail(errors)
                signal.countDown()
            }
        })

        signal.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun bad_card_fails_tokenization() {
        val signal = CountDownLatch(1)
        val card = CreditCard.testCreditCard()
        card.addressZip = ""
        card.personName = ""

        // Make the tokenization request
        client.tokenize(card, object : FattmerchantClient.TokenizationListener {
            override fun onPaymentMethodCreated(paymentMethod: PaymentMethod) {
                fail("Tokenized a card that was supposed to fail")
                signal.countDown()
            }

            override fun onPaymentMethodCreateError(errors: String) {
                assert(errors.contains("zip"))
                signal.countDown()
            }
        })

        signal.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun can_tokenize_bank_account() {
        val signal = CountDownLatch(1)
        val bank = BankAccount.testBankAccount()

        // Make the tokenization request
        client.tokenize(bank, object : FattmerchantClient.TokenizationListener {
            override fun onPaymentMethodCreated(paymentMethod: PaymentMethod) {
                assertEquals(paymentMethod.personName, bank.personName)
                assertEquals(paymentMethod.method, "bank")
                assertNotNull(paymentMethod.id)
                signal.countDown()
            }

            override fun onPaymentMethodCreateError(errors: String) {
                fail(errors)
                signal.countDown()
            }
        })

        signal.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun bad_bank_account_fails_tokenization() {
        val signal = CountDownLatch(1)
        val bank = BankAccount.testBankAccount()
        bank.bankRouting = ""
        bank.bankAccount = "9"

        // Make the tokenization request
        client.tokenize(bank, object : FattmerchantClient.TokenizationListener {
            override fun onPaymentMethodCreated(paymentMethod: PaymentMethod) {
                fail("Tokenized a bank account that was supposed to fail")
                signal.countDown()
            }

            override fun onPaymentMethodCreateError(errors: String) {
                assert(errors.contains("routing"))
                signal.countDown()
            }
        })

        signal.await(5, TimeUnit.SECONDS)
    }
}