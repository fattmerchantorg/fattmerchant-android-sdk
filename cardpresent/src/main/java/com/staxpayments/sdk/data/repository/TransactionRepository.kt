package com.staxpayments.sdk.data.repository

import com.staxpayments.sdk.data.models.OmniException
import com.staxpayments.sdk.data.models.Transaction

internal interface TransactionRepository : ModelRepository<Transaction> {

    class CreateTransactionException(message: String? = null) : OmniException("Could not create transaction", message)
    class GetTransactionException(message: String? = null) : OmniException("Could not get transactions", message)

    override suspend fun create(model: Transaction, error: (OmniException) -> Unit): Transaction? =
        omniApi.createTransaction(model) {
            error(CreateTransactionException(it.message))
        }

    override suspend fun get(error: (OmniException) -> Unit): List<Transaction>? = omniApi.getTransactions {
        error(GetTransactionException(it.message))
    }

    override suspend fun update(model: Transaction, error: (OmniException) -> Unit): Transaction? {
        return omniApi.updateTransaction(model)
    }
}
