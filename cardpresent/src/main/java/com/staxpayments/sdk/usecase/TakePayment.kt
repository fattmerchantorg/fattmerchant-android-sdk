package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.TransactionRequest
import com.staxpayments.api.models.ChargeRequest
import com.staxpayments.api.models.Transaction
import com.staxpayments.api.repository.CustomerRepository
import com.staxpayments.api.repository.PaymentMethodRepository
import com.staxpayments.api.StaxApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

internal class TakePayment(
    val customerRepository: CustomerRepository,
    val paymentMethodRepository: PaymentMethodRepository,
    val request: TransactionRequest,
    val staxApi: StaxApi,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(failure: (StaxException) -> Unit): Transaction? = coroutineScope {

        if (request.card == null) {
            failure(StaxException("No payment method provided."))
            return@coroutineScope null
        }

        val tokenizeJob = TokenizePaymentMethod(
            customerRepository = customerRepository,
            paymentMethodRepository = paymentMethodRepository,
            creditCard = request.card,
            coroutineContext = coroutineContext
        )

        val tokenizedPaymentMethod = tokenizeJob.start {
            failure(it)
        } ?: return@coroutineScope null

        tokenizedPaymentMethod.id?.let {
            val chargeRequest = createChargeRequest(request.amount, it)
            staxApi.charge(chargeRequest) {
                failure(StaxException("Charging the payment method was unsuccessful."))
            }
        } ?: return@coroutineScope null
    }

    private fun createChargeRequest(amount: Amount, paymentMethodId: String): ChargeRequest {
        val chargeRequestMeta = mapOf("subtotal" to amount.dollarsString())
        return ChargeRequest(paymentMethodId, amount.dollarsString(), false, chargeRequestMeta)
    }
}
