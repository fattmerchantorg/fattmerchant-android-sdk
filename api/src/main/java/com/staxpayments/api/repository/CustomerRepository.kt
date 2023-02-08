package com.staxpayments.api.repository

import com.staxpayments.api.models.Customer
import com.staxpayments.api.models.request.CustomerRequest

interface CustomerRepository {

    suspend fun getCustomerById(id: String): Customer

    suspend fun createCustomer(customer: CustomerRequest): Customer

    suspend fun updateCustomer(customer: CustomerRequest, id: String): Customer
}
