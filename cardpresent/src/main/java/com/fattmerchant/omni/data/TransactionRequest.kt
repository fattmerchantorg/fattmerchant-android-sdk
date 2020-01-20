package com.fattmerchant.omni.data

/**
 * A request for a transaction
 *
 * Has all necessary information to perform a transaction
 */
data class TransactionRequest(
    val amount: Amount
)