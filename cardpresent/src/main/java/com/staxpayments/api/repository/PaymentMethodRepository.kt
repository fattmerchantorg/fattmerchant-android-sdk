package com.staxpayments.api.repository

import com.staxpayments.api.models.PaymentMethod
import com.staxpayments.exceptions.StaxException

internal interface PaymentMethodRepository : ModelRepository<PaymentMethod> {

    class CreatePaymentMethodException(message: String? = null) :
        StaxException("Could not create payment method", message)

    override suspend fun create(model: PaymentMethod, error: (StaxException) -> Unit): PaymentMethod? {
        return staxApi.createPaymentMethod(model) {
            error(CreatePaymentMethodException(it.message))
        }
    }
}
