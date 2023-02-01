package com.staxpayments.api.datasource

import com.staxpayments.api.models.Customer
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.CustomerRepository

class CustomerLiveRepository(private val networkClients: NetworkClient) : CustomerRepository {
    override suspend fun customer(): Customer =
        networkClients.get("https://api.", responseType = Customer::class.java)
}
