package com.staxpayments.sdk.data.repository

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.models.Customer

internal interface CustomerRepository : ModelRepository<Customer> {

    class CreateCustomerException(message: String? = null) : StaxException("Could not create customer", message)
    class GetCustomerException(message: String? = null) : StaxException("Could not get customer", message)

    override suspend fun create(model: Customer, error: (StaxException) -> Unit): Customer? {
        return staxApi.createCustomer(model) {
            error(CreateCustomerException(it.message))
        }
    }

    override suspend fun getById(id: String, error: (StaxException) -> Unit): Customer? {
        return staxApi.getCustomer(id) {
            error(GetCustomerException(it.message))
        }
    }
}
