package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.models.*

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

    /** The id of the [Customer] we want to apply the [Transaction] to
     *
     * Note that if there is a payment method created
     * */
    var customerId: String? = null,

    /** The [CreditCard] to charge */
    var card: CreditCard? = null,

    /** The [BankAccount] to charge */
    var bankAccount: BankAccount? = null,

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

    /** The shipping amount applied to the transaction */
    var shippingAmount: Double? = null,

    /** The purchase order number for the transaction */
    var poNumber: String? = null,

    /** A memo for the transaction */
    var memo: String? = null,

    /** A reference for the transaction */
    var reference: String? = null,

    /**
     * Credit or Debit
     *
     * Currently, this is only supported for integrated terminals
     *  */
    var paymentType: PaymentType? = PaymentType.CREDIT,

    /**
     * The option to perform a preauthorization
     *
     *  Set this to true if you would like to *only* authorize an amount. This means that the
     *  transaction will only hold funds and you will need to capture it at a later date via the
     *  Stax API or the SDK
     */
    var preauth: Boolean = false,

    /**
     * Metadata that you want to pass in with the transaction. This will be put into the transaction
     * record along with any other metadata that this SDK already adds to the transaction meta
     */
    var meta: Map<String, Any?>? = null,

    /**
     * The id that we want the transaction to have
     *
     * We also give this to the 3rd party gateway to assign to the transaction so we know how to
     * reference it later. Not all gateways support this
     */
    internal var transactionId: String? = null
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
    constructor(amount: Amount, creditCard: CreditCard) : this(amount, true, null, null, creditCard)

    /**
     * Initializes a Transaction with the given [Amount] and [BankAccount]
     *
     * Note that this will request tokenization
     *
     * @param amount The [Amount] to be collected during the transaction
     * @param bankAccount The [BankAccount] used for the transaction
     * */
    constructor(amount: Amount, bankAccount: BankAccount) : this(amount, true, bankAccount = bankAccount)

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
    constructor(amount: Amount, lineItems: List<CatalogItem>?) :
        this(amount, true, lineItems = lineItems)
}
