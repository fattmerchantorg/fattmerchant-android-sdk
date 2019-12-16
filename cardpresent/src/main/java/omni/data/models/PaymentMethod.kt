package omni.data.models

/**
 * An Omni Payment Method
 *
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
    open var meta: String? = null
    open var method: String? = null
    open var nickname: String? = null
    open var personName: String? = null
    open var purgedAt: String? = null
    open var spreedlyToken: String? = null
    open var tokenize: Boolean? = null
    open var updatedAt: String? = null
}