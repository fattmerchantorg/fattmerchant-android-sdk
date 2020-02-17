package com.fattmerchant.omni.data

/**
 * A request for a transaction
 *
 * Has all necessary information to perform a transaction
 */
data class TransactionRequest(
    /** The [Amount] to be collected during the transaction */
    val amount: Amount,

    /** The option to tokenize the payment method for later usage */
    val tokenize: Boolean = true
) {

    /**
     * Initializes a Transaction with the given amount
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * */
    constructor(amount: Amount) : this(amount, true)
}