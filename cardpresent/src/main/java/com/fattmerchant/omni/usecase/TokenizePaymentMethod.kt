package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.models.*
import com.fattmerchant.omni.data.repository.CustomerRepository
import com.fattmerchant.omni.data.repository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

internal class TokenizePaymentMethod(
        val customerRepository: CustomerRepository,
        val paymentMethodRepository: PaymentMethodRepository,
        val creditCard: CreditCard? = null,
        val bankAccount: BankAccount? = null,
        override val coroutineContext: CoroutineContext) : CoroutineScope {

    suspend fun start(failure: (OmniException) -> Unit): PaymentMethod? = coroutineScope {
        var firstName: String? = null
        var lastName: String? = null
        var customer: Customer? = null

        creditCard?.personName?.let {
            firstName = creditCard.firstName()
            lastName = creditCard.lastName()
        }.run {
            bankAccount?.personName?.let {
                firstName = bankAccount.firstName()
                lastName = bankAccount.lastName()
            }?.run {
                failure(OmniException("No name supplied."))
                return@coroutineScope null
            }
        }

        creditCard?.customerId?.let {
            customer = customerRepository.getById(it) { exception ->
                failure(exception)
            } ?: return@coroutineScope null
        }?.run {
            customer = customerRepository.create(Customer().apply {
                this.firstname =  firstName?: "SWIPE"
                this.lastname = lastName?: "CUSTOMER"
            }) { exception ->
                failure(exception)
            } ?: return@coroutineScope null
        }

        var paymentMethod = PaymentMethod()

        creditCard?.let { card ->
            paymentMethod = PaymentMethod().apply {
                merchantId = customer?.merchantId
                customerId = customer?.id
                method = "card"
                this.cardLastFour = cardLastFour
                personName = card.personName
                cardNumber = card.cardNumber
                tokenize = true
            }
        }.run {
            bankAccount?.let {  bank ->
                paymentMethod = PaymentMethod().apply {
                    merchantId = customer?.merchantId
                    customerId = customer?.id
                    method = "bank"
                    bankAccount = bank.bankAccount
                    bankRouting = bank.bankRouting
                    bankName = bank.bankName
                    bankType = bank.bankType
                    personName = bank.personName
                    tokenize = true
                }
            }?.run {
                failure(OmniException("No credit card or bank information was supplied."))
                return@coroutineScope null
            }
        }

        paymentMethodRepository.create(paymentMethod) { exception ->
            failure(exception)
        }
    }
}