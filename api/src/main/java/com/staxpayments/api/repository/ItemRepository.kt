package com.staxpayments.api.repository

import com.staxpayments.api.models.Item
import com.staxpayments.api.requests.ItemRequest

interface ItemRepository {

    suspend fun getItemById(id: String): Item

    suspend fun createItem(item: ItemRequest): Item

    suspend fun updateItem(item: ItemRequest, id: String): Item
}
