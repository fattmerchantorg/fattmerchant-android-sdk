//package com.fattmerchant.cpresent.android.payment_method
//
//import com.fattmerchant.cpresent.android.api.OmniApi
//import com.fattmerchant.cpresent.omni.entity.models.PaymentMethod as OmniPaymentMethod
//import com.fattmerchant.cpresent.omni.entity.repository.PaymentMethodRepository
//import timber.log.Timber
//
//class PaymentMethodRepository(
//    val omniApi: OmniApi
//) : PaymentMethodRepository {
//    override suspend fun create(model: OmniPaymentMethod): OmniPaymentMethod {
//
//        try {
//            return omniApi.createPaymentMethod(PaymentMethod.fromOmniPaymentMethod(model))
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//        return model.apply { id = "123456" }
//    }
//
//    override suspend fun update(model: OmniPaymentMethod): OmniPaymentMethod {
//        return model.apply { id = "updated" }
//    }
//
//    override suspend fun delete(model: OmniPaymentMethod): Boolean {
//        return true
//    }
//
//    override suspend fun getById(id: String): OmniPaymentMethod {
//        return OmniPaymentMethod().apply { this.id = id }
//    }
//
//    override suspend fun createPaymentMethod(maskedPan: String): OmniPaymentMethod {
//        return create(OmniPaymentMethod().apply {
//            id = "123456"
//            this.cardLastFour = maskedPan
//        })
//    }
//
//}