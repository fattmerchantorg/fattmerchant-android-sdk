package com.staxpayments.api.repository

import com.staxpayments.api.models.Item

interface ItemRepository {

    suspend fun getItemById(id: String): Item

    suspend fun createItem(item: Item): Item

    suspend fun updateItem(item: Item, id: String): Item
}
