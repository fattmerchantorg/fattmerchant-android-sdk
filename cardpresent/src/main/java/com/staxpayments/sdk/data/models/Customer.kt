package com.staxpayments.sdk.data.models

open class Customer : Model {
    override var id: String? = null
    open var address1: String? = null
    open var address2: String? = null
    open var addressCity: String? = null
    open var addressCountry: String? = null
    open var addressState: String? = null
    open var addressZip: String? = null
    open var allowInvoiceCreditCardPayments: Boolean? = null
    open var ccEmails: List<String>? = null
    open var ccSms: List<String>? = null
    open var company: String? = null
    open var createdAt: String? = null
    open var deletedAt: String? = null
    open var email: String? = null
    open var firstname: String? = null
    open var lastname: String? = null
    open var merchantId: String? = null
    open var notes: String? = null
    open var phone: String? = null
    open var reference: String? = null
    open var updatedAt: String? = null
}
