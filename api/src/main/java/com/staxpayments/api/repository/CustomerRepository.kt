package com.staxpayments.api.repository

import com.staxpayments.api.models.Customer

interface CustomerRepository {
   suspend fun customer(): Customer
}