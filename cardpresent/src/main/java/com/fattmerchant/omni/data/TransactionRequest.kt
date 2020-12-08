package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.models.CatalogItem
import com.fattmerchant.omni.data.models.CreditCard

/**
 * A request for a transaction
 *
 * Has all necessary information to perform a transaction
 */
data class TransactionRequest(
    /** The [Amount] to be collected during the transaction */
    var amount: Amount,

    /** The option to tokenize the payment method for later usage */
    var tokenize: Boolean = true,

    /** The id of the invoice we want to apply the transaction to */
    var invoiceId: String? = null,

    /** The [CreditCard] to charge */
    var card: CreditCard? = null,

    /** A list of [CatalogItem]s to associate with the [Transaction]
     *
     * - The [CatalogItem]s do not need to currently exist within Omni
     * - This will **not** update the [CatalogItem.item], [CatalogItem.quantity], [CatalogItem.price]
     * or [CatalogItem.details] within Omni.
     * - Passing a [CatalogItem] with an id of an existing CatalogItem affects the amount in stock
     * of that item within Omni. For example, if you CatalogItem in Omni has amountInStock = 100,
     * and you submit a TransactionRequest with that CatalogItem.quantity = 9, then the
     * amountInStock of the CatalogItem in Omni will be decremented by to reflect a final
     * amountInStock of 91.
     * */
    var lineItems: List<CatalogItem>? = listOf(),

    /** The subtotal of the transaction */
    var subtotal: Double? = null,

    /** The tax applied to the transaction */
    var tax: Double? = null,

    /** The tip amount applied to the transaction */
    var tip: Double? = null,

    /** A memo for the transaction */
    var memo: String? = null,

    /** A reference for the transaction */
    var reference: String? = null
) {

    /**
     * Initializes a Transaction with the given [Amount]
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * */
    constructor(amount: Amount) : this(amount, true)

    /**
     * Initializes a Transaction with the given [Amount] and [CreditCard]
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * @param creditCard The [CreditCard] used for the transaction
     * */
    constructor(amount: Amount, creditCard: CreditCard) : this(amount, true, null, creditCard)

    /**
     * Initializes a Transaction with the given [Amount] and invoiceId
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * @param invoiceId The id of the invoice we are applying the transaction to
     * */
    constructor(amount: Amount, invoiceId: String?) : this(amount, true, invoiceId)

    /**
     * Initializes a Transaction with the given [Amount] and list of [CatalogItem]s
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * @param lineItems The [CatalogItem]s to be added to the transaction
     * */
    constructor(amount: Amount, lineItems: List<CatalogItem>?)
            : this(amount, true, null, null, lineItems)
}