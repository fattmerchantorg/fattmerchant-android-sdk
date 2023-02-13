package com.staxpayments.api.repository

import com.staxpayments.api.models.Transaction

interface TransactionRepository {

    suspend fun getTransactionById(id: String): Transaction

    suspend fun charge(transaction: Transaction): Transaction
}
