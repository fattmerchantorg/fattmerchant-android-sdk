package com.fattmerchant.omni.data

/**
 * Represents a request for a mobile reader payment
 *
 * This includes important data like the cardholder name, amount, and auth code.
 */
open class TransactionResult {

    /** The [TransactionRequest] that initiated the transaction that gave this result */
    var request: TransactionRequest? = null

    /** True when transaction succeeded, false otherwise */
    var success: Boolean? = false

    /**
     * The masked card number
     *
     * This is *not* the entire card number! The card number will have a mask on so only the last four digits are shown
     *
     * For example: "4111111111111111" should be "************1111"
     * */
    internal var maskedPan: String? = null

    /** The first name of the cardholder */
    var cardHolderFirstName: String? = null

    /** The last name of the cardholder */
    var cardHolderLastName: String? = null

    /** The code that authorizes the sale */
    var authCode: String? = null

    /** Sale, Refund, etc */
    var transactionType: String? = null

    /** [Amount] of money that was exchanged */
    var amount: Amount? = null

    /** VISA, Mastercard, JCB, etc */
    var cardType: String? = null

    /** The expiration date of the card in 'mmyy' format */
    var cardExpiration: String? = null

    /** A user-defined string used to refer to the transaction */
    var userReference: String? = null

    /**
     * The ID of this transaction in the local database
     *
     * For example, in the case of NMI, this would be the CardEaseReferenceId
     */
    var localId: String? = null

    /**
     * The ID of this transaction in the 3rd party responsible for performing it.
     *
     * For example, in the case of NMI, this would be the TransactionID
     */
    var externalId: String? = null

    /** The token that represents this payment method */
    internal var paymentToken: String? = null

}
