package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.ChargeRequest
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.CustomerRepository
import com.fattmerchant.omni.data.repository.PaymentMethodRepository
import com.fattmerchant.omni.networking.OmniApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

internal class TakePayment(val customerRepository: CustomerRepository,
                           val paymentMethodRepository: PaymentMethodRepository,
                           val request: TransactionRequest,
                           val omniApi: OmniApi,
                           override val coroutineContext: CoroutineContext) : CoroutineScope {

    suspend fun start(failure: (OmniException) -> Unit): Transaction? = coroutineScope {

        if(request.card == null) {
            failure(OmniException("No payment method provided."))
            return@coroutineScope null
        }

        val tokenizeJob = TokenizePaymentMethod(
                customerRepository = customerRepository,
                paymentMethodRepository = paymentMethodRepository,
                creditCard = request.card,
                coroutineContext = coroutineContext)

        val tokenizedPaymentMethod = tokenizeJob.start {
            failure(it)
        }?: return@coroutineScope null

        tokenizedPaymentMethod.id?.let {
            val chargeRequest = createChargeRequest(request.amount, it)
            omniApi.charge(chargeRequest) { error ->
                failure(OmniException("Charging the payment method was unsuccessful."))
            }
        }?: return@coroutineScope null
    }

    private fun createChargeRequest(amount: Amount, paymentMethodId: String): ChargeRequest {
        val chargeRequestMeta = mapOf("subtotal" to amount.dollarsString())
        return ChargeRequest(paymentMethodId, amount.dollarsString(), false, chargeRequestMeta)
    }
}