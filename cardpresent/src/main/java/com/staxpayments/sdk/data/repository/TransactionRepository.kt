package com.staxpayments.sdk.data.repository

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.models.Transaction

internal interface TransactionRepository : ModelRepository<Transaction> {

    class CreateTransactionException(message: String? = null) : StaxException("Could not create transaction", message)
    class GetTransactionException(message: String? = null) : StaxException("Could not get transactions", message)

    override suspend fun create(model: Transaction, error: (StaxException) -> Unit): Transaction? =
        staxApi.createTransaction(model) {
            error(CreateTransactionException(it.message))
        }

    override suspend fun get(error: (StaxException) -> Unit): List<Transaction>? = staxApi.getTransactions {
        error(GetTransactionException(it.message))
    }

    override suspend fun update(model: Transaction, error: (StaxException) -> Unit): Transaction? {
        return staxApi.updateTransaction(model)
    }
}
