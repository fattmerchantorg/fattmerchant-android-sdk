package com.staxpayments.sdk.data.models

/**
 * A Stax Payment Method
 */
open class PaymentMethod : Model {
    override var id: String? = null
    open var address1: String? = null
    open var address2: String? = null
    open var addressCity: String? = null
    open var addressCountry: String? = null
    open var addressState: String? = null
    open var addressZip: String? = null
    open var bankHolderType: String? = null
    open var bankName: String? = null
    open var bankType: String? = null
    open var cardExp: String? = null
    open var cardExpDatetime: Any? = null
    open var cardLastFour: String? = null
    open var cardType: String? = null
    open var createdAt: String? = null
    open var customerId: String? = null
    open var deletedAt: String? = null
    open var hasCvv: Boolean? = null
    open var isDefault: Int? = null
    open var isUsableInVt: Boolean? = null
    open var merchantId: String? = null
    open var meta: Any? = null
    open var method: String? = null
    open var nickname: String? = null
    open var personName: String? = null
    open var purgedAt: String? = null
    open var spreedlyToken: String? = null
    open var updatedAt: String? = null

    /**
     * Whether or not Stax should tokenize this PaymentMethod
     *
     * @note If this field is true, `paymentToken` must be `null`
     */
    open var tokenize: Boolean? = null

    /**
     * The token that represents this payment method
     *
     * The only use-case for this field is storing the token within Stax.
     * After cardpresent tokenization, we can create a PaymentMethod using this class.
     * If we include the paymentToken, then we can later store it as an already-tokenized
     * PaymentMethod
     *
     * Stax performs transactions with this token. Therefore, it is crucial that only the actual
     * payment token be placed here
     *
     * @note If this field is not `null`, then `tokenize` must be `false`
     */
    internal var paymentToken: String? = null

    /**
     * The card number
     */
    internal var cardNumber: String? = null

    /**
     * The card CVV
     */
    internal var cardCvv: String? = null

    /**
     * The bank account number
     */
    internal var bankAccount: String? = null

    /**
     * The bank routing number
     */
    internal var bankRouting: String? = null
}
