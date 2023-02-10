package com.staxpayments.api.datasource

import com.staxpayments.api.models.Item
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.ItemRepository

class ItemLiveRepository(private val networkClients: NetworkClient) : ItemRepository {

    override suspend fun getItemById(id: String): Item =
        networkClients.get("item/$id", responseType = Item.serializer())

    override suspend fun createItem(item: Item): Item =
        networkClients.post("item", request = item, responseType = Item.serializer())

    override suspend fun updateItem(item: Item, id: String): Item =
        networkClients.put("item/$id", request = item, responseType = Item.serializer())
}
