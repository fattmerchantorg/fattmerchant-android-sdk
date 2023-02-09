package com.staxpayments.api.datasource

import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.UserRepository
import com.staxpayments.api.responses.UserResponse

class UserLiveRepository(
    private val networkClients: NetworkClient
) : UserRepository {

    override suspend fun getUser(): UserResponse {
        return networkClients.get("self", responseType = UserResponse.serializer())
    }
}