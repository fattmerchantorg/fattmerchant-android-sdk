package com.fattmerchant.cpresent.omni.entity

import com.fattmerchant.cpresent.omni.entity.models.Transaction

interface MobileReaderDriver {
    suspend fun initialize(args: Map<String, Any>): Boolean
    suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader>
    suspend fun connectReader(reader: MobileReader): Boolean
    suspend fun isReadyToTakePayment(): Boolean
    suspend fun performTransaction(request: TransactionRequest): TransactionResult
    suspend fun voidTransaction(transaction: Transaction): TransactionResult
    suspend fun refundTransaction(transaction: Transaction): TransactionResult
}