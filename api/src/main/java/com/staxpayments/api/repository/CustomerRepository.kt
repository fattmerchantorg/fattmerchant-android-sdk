package com.staxpayments.api.repository

import com.staxpayments.api.models.Customer

interface CustomerRepository {

    suspend fun getCustomerById(id: String): Customer

    suspend fun createCustomer(customer: Customer): Customer

    suspend fun updateCustomer(customer: Customer, id: String): Customer
}
