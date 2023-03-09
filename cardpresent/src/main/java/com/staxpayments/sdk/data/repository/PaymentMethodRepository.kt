package com.staxpayments.sdk.data.repository

import com.staxpayments.sdk.data.models.OmniException
import com.staxpayments.sdk.data.models.PaymentMethod

internal interface PaymentMethodRepository : ModelRepository<PaymentMethod> {

    class CreatePaymentMethodException(message: String? = null) :
        OmniException("Could not create payment method", message)

    override suspend fun create(model: PaymentMethod, error: (OmniException) -> Unit): PaymentMethod? {
        return omniApi.createPaymentMethod(model) {
            error(CreatePaymentMethodException(it.message))
        }
    }
}
