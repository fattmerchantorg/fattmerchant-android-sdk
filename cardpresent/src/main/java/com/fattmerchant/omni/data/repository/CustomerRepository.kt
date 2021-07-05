package com.fattmerchant.omni.data.repository

import com.fattmerchant.omni.data.models.Customer
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.OmniException

internal interface CustomerRepository : ModelRepository<Customer> {

    class CreateCustomerException(message: String? = null) : OmniException("Could not create customer", message)
    class GetCustomerException(message: String? = null) : OmniException("Could not get customer", message)

    override suspend fun create(model: Customer, error: (OmniException) -> Unit): Customer? {
        return omniApi.createCustomer(model) {
            error(CreateCustomerException(it.message))
        }
    }

    override suspend fun getById(id: String, error: (OmniException) -> Unit): Customer? {
        return omniApi.getCustomer(id) {
            error(GetCustomerException(it.message))
        }
    }

}