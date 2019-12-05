package com.fattmerchant.cpresent.omni.entity

/**
 * A request for a transaction
 *
 * Has all necessary information to perform a transaction
 */
data class TransactionRequest(
    val amount: Amount,
    val type: TransactionType
)