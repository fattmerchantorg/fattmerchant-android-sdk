package com.staxpayments.api.repository

import com.staxpayments.api.models.PaymentMethod

interface PaymentMethodRepository {

    suspend fun getPaymentMethod(paymentMethodId: String): PaymentMethod
}
