package com.fattmerchant.cpresent.omni.entity.repository

import com.fattmerchant.cpresent.omni.entity.models.PaymentMethod

interface PaymentMethodRepository: ModelRepository<PaymentMethod> {

    override suspend fun create(model: PaymentMethod, error: (Error) -> Unit): PaymentMethod? {
        return omniApi.createPaymentMethod(model, error)
    }
}