package com.staxpayments.api.datasource

import com.staxpayments.api.models.Customer
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.CustomerRepository

class CustomerLiveRepository(private val networkClients: NetworkClient) : CustomerRepository {

    override suspend fun getCustomerById(id: String): Customer =
        networkClients.get("customer/$id", responseType = Customer.serializer())

    override suspend fun createCustomer(customer: Customer): Customer =
        networkClients.post("customer", request = customer, responseType = Customer.serializer())

    override suspend fun updateCustomer(customer: Customer, id: String): Customer =
        networkClients.put("customer/$id", request = customer, responseType = Customer.serializer())
}
