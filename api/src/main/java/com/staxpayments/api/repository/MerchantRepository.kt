package com.staxpayments.api.repository

import com.staxpayments.api.models.Merchant

interface MerchantRepository {

    suspend fun getMerchant(merchantId: String): Merchant
}
