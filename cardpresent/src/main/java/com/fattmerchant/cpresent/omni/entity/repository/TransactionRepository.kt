package com.fattmerchant.cpresent.omni.entity.repository

import com.fattmerchant.cpresent.omni.entity.Amount
import com.fattmerchant.cpresent.omni.entity.models.Transaction

interface TransactionRepository : ModelRepository<Transaction> {

    override suspend fun create(model: Transaction, error: (Error) -> Unit): Transaction? =
        omniApi.createTransaction(model, error)

    override suspend fun get(error: (Error) -> Unit): List<Transaction>? = omniApi.getTransactions(error)

//    suspend fun create(
//        paymentMethodId: String,
//        total: Amount,
//        source: String,
//        success: Boolean,
//        type: String,
//        lastFour: String,
//        message: String,
//        method: String,
//        customerId: String,
//        invoiceId: String,
//        gatewayResponse: String? = null
//    ) = create(
//        Transaction().apply {
//            this.paymentMethodId = paymentMethodId
//            this.total = total.dollarsString()
//            this.type = type
//            this.success = success
//            this.lastFour = lastFour
//            this.message = message
//            this.method = method
//            this.customerId = customerId
//            this.invoiceId = invoiceId
//        }
//    )
}