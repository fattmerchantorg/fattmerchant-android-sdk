package com.staxpayments.api.repository

import com.staxpayments.api.responses.UserResponse

interface UserRepository {

    suspend fun getUser(): UserResponse
}
