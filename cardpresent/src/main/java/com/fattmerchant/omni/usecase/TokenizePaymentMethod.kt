package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.models.*
import com.fattmerchant.omni.data.repository.CustomerRepository
import com.fattmerchant.omni.data.repository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

/// Tokenizes a payment method
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

        creditCard?.let {
            firstName = it.firstName()
            lastName = it.lastName()
        }.run {
            bankAccount?.let {
                firstName = it.firstName()
                lastName = it.lastName()
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
                this.firstname =  firstName?: "NO"
                this.lastname = lastName?: "NAME"
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