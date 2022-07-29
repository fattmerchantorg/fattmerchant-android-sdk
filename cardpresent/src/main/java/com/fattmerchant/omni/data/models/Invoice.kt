package com.fattmerchant.omni.data.models

/**
 * An Omni invoice
 *
 */
open class Invoice : Model {
    override var id: String? = null
    open var url: String? = null
    open var customerId: String? = null
    open var meta: Map<String, Any>? = null
    open var paymentMethodId: String? = null
    open var total: String? = null

//    open var balanceDue: String? = null
//    open var createdAt: String? = null
//    open var deletedAt: String? = null
//    open var dueAt: String? = null
//    open var isMerchantPresent: Boolean? = null
//    open var isPartialPaymentEnabled: Boolean? = null
//    open var isWebpayment: Boolean? = null
//    open var merchantId: String? = null
//    open var paidAt: String? = null
//    open var paymentAttemptFailed: Boolean? = null
//    open var paymentAttemptMessage: String? = null
//    open var paymentMeta: Any? = null
//    open var scheduleId: String? = null
//    open var sentAt: String? = null
//    open var status: String? = null
//    open var totalPaid: String? = null
//    open var updatedAt: String? = null
//    open var userId: String? = null
//    open var viewedAt: String? = null
}
