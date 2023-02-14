package com.staxpayments.api.repository

import com.staxpayments.api.models.Merchant

interface MerchantRepository {

    suspend fun getMerchantById(id: String): Merchant
}
