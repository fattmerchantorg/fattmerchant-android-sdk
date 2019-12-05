package com.fattmerchant.cpresent.android.customer

import com.fattmerchant.cpresent.omni.entity.models.Customer as OmniCustomer
import com.squareup.moshi.Json

class Customer: OmniCustomer() {

    companion object {
        fun fromOmniCustomer(c: OmniCustomer): Customer {
            return Customer().apply {
                id = c.id
                merchantId = c.merchantId
                firstname = c.firstname
                lastname = c.lastname
                email = c.email
                ccEmails = c.ccEmails
                ccSms = c.ccSms
                phone = c.phone
                company = c.company
                address1 = c.address1
                address2 = c.address2
                addressCity = c.addressCity
                addressState = c.addressState
                addressZip = c.addressZip
                addressCountry = c.addressCountry
                notes = c.notes
                reference = c.reference
                allowInvoiceCreditCardPayments = c.allowInvoiceCreditCardPayments
                deletedAt = c.deletedAt
                createdAt = c.createdAt
                updatedAt = c.updatedAt
            }
        }
    }

    @Json(name = "merchant_id")
    override var merchantId: String? = null

    @Json(name = "cc_emails")
    override var ccEmails: String? = null

    @Json(name = "cc_sms")
    override var ccSms: String? = null

    @Json(name = "address_1")
    override var address1: String? = null

    @Json(name = "address_2")
    override var address2: String? = null

    @Json(name = "address_city")
    override var addressCity: String? = null

    @Json(name = "address_state")
    override var addressState: String? = null

    @Json(name = "address_zip")
    override var addressZip: String? = null

    @Json(name = "address_country")
    override var addressCountry: String? = null

    @Json(name = "allow_invoice_credit_card_payments")
    override var allowInvoiceCreditCardPayments: Boolean? = null

    @Json(name = "deleted_at")
    override var deletedAt: String? = null

    @Json(name = "created_at")
    override var createdAt: String? = null

    @Json(name = "updated_at")
    override var updatedAt: String? = null
}