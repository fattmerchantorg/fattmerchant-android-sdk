package com.fattmerchant.omni.data.repository

import com.fattmerchant.omni.data.models.Customer
import com.fattmerchant.omni.data.models.OmniException

interface CustomerRepository : ModelRepository<Customer> {

    class CreateCustomerException(message: String? = null) : OmniException("Could not create customer", message)

    override suspend fun create(model: Customer, error: (OmniException) -> Unit): Customer? {
        return omniApi.createCustomer(model) {
            error(CreateCustomerException(it.message))
        }
    }

}