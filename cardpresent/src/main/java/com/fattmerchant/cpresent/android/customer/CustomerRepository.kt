//package com.fattmerchant.cpresent.android.customer
//
//import com.fattmerchant.cpresent.android.api.OmniApi
//import com.fattmerchant.cpresent.omni.entity.models.Customer as OmniCustomer
//import com.fattmerchant.cpresent.omni.entity.repository.CustomerRepository
//import timber.log.Timber
//
//class CustomerRepository(
//    val omniApi: OmniApi
//) : CustomerRepository {
//
//    override suspend fun createCustomer(): OmniCustomer {
//        var customerToSave = Customer().apply {
//            firstname = "Newtulio"
//            lastname = "Newtroncoso"
//            phone = "3059263759"
//        }
//
//        try {
//            val saved = omniApi.createCustomer(customerToSave)
//            return saved
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//
//        return OmniCustomer()
//    }
//
//    override suspend fun create(model: OmniCustomer): OmniCustomer {
//        return omniApi.createCustomer(Customer.fromOmniCustomer(model))
//    }
//
//    override suspend fun update(model: OmniCustomer): OmniCustomer {
//        return model.apply { id = "234" }
//    }
//
//    override suspend fun delete(model: OmniCustomer): Boolean {
//        return true
//    }
//
//    override suspend fun getById(id: String): OmniCustomer {
//        return OmniCustomer().apply { this.id = id }
//    }
//}