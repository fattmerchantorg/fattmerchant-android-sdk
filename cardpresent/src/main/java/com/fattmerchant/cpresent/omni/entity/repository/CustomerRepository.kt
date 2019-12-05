package com.fattmerchant.cpresent.omni.entity.repository

import com.fattmerchant.cpresent.omni.entity.models.Customer

interface CustomerRepository : ModelRepository<Customer> {

    override suspend fun create(model: Customer, error: (Error) -> Unit): Customer? {
        return omniApi.createCustomer(model, error)
    }

}