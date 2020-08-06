package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.models.CatalogItem

/**
 * A request for a transaction
 *
 * Has all necessary information to perform a transaction
 */
data class TransactionRequest(
    /** The [Amount] to be collected during the transaction */
    val amount: Amount,

    /** The option to tokenize the payment method for later usage */
    val tokenize: Boolean = true,

    val lineItems: List<CatalogItem>? = listOf()
) {

    /**
     * Initializes a Transaction with the given amount
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * */
    constructor(amount: Amount) : this(amount, true)

    /**
     * Initializes a Transaction with the given amount and list of catalog items
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * @param lineItems The [CatalogItem]s to be added to the transaction
     * */
    constructor(amount: Amount, lineItems: List<CatalogItem>?) : this(amount, true, lineItems)
}