package com.staxpayments.api.datasource

import com.staxpayments.api.models.Merchant
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.MerchantRepository

class MerchantLiveRepository(
    private val networkClients: NetworkClient
) : MerchantRepository {

    override suspend fun getMerchantById(id: String): Merchant {
        return networkClients.get("merchant/$id", responseType = Merchant.serializer())
    }
}
