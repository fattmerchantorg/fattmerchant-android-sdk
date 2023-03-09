package com.staxpayments.sdk.data.repository

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.models.PaymentMethod

internal interface PaymentMethodRepository : ModelRepository<PaymentMethod> {

    class CreatePaymentMethodException(message: String? = null) :
        StaxException("Could not create payment method", message)

    override suspend fun create(model: PaymentMethod, error: (StaxException) -> Unit): PaymentMethod? {
        return staxApi.createPaymentMethod(model) {
            error(CreatePaymentMethodException(it.message))
        }
    }
}
