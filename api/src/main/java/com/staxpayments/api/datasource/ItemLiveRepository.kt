package com.staxpayments.api.datasource

import com.staxpayments.api.models.Item
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.ItemRepository
import com.staxpayments.api.requests.ItemRequest

class ItemLiveRepository(private val networkClients: NetworkClient) : ItemRepository {

    override suspend fun getItemById(id: String): Item =
        networkClients.get("item/$id", responseType = Item.serializer())

    override suspend fun createItem(item: ItemRequest): Item =
        networkClients.post("item", request = item, responseType = Item.serializer())

    override suspend fun updateItem(item: ItemRequest, id: String): Item =
        networkClients.put("item/$id", request = item, responseType = Item.serializer())
}
