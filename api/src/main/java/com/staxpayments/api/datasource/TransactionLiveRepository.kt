package com.staxpayments.api.datasource

import com.staxpayments.api.models.Transaction
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.TransactionRepository

class TransactionLiveRepository(private val networkClients: NetworkClient) : TransactionRepository {

    override suspend fun getTransactionById(id: String): Transaction =
        networkClients.get("transaction/$id", responseType = Transaction.serializer())

    override suspend fun charge(transaction: Transaction): Transaction =
        networkClients.post("charge", request = transaction, responseType = Transaction.serializer())
}
