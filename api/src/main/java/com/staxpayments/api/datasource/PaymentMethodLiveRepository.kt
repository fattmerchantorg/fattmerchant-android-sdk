package com.staxpayments.api.datasource

import com.staxpayments.api.models.PaymentMethod
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.PaymentMethodRepository

class PaymentMethodLiveRepository(
    private val networkClients: NetworkClient
) : PaymentMethodRepository {

    override suspend fun getPaymentMethod(paymentMethodId: String): PaymentMethod {
        return networkClients.get("payment-method/$paymentMethodId", responseType = PaymentMethod.serializer())
    }
}
