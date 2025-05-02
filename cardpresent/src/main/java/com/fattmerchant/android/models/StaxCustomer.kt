package com.fattmerchant.android.models

import com.fattmerchant.android.serialization.Utc
import com.squareup.moshi.Json
import java.util.Date

data class StaxCustomer(
    val id: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val company: String? = null,
    val email: String? = null,

    @Json(name = "cc_emails")
    val ccEmails: List<String>? = null,

    @Json(name = "cc_sms")
    val ccSms: List<String>? = null,

    val phone: String? = null,

    @Json(name = "address_1")
    val address1: String? = null,

    @Json(name = "address_2")
    val address2: String? = null,

    @Json(name = "address_city")
    val addressCity: String? = null,

    @Json(name = "address_state")
    val addressState: String? = null,

    @Json(name = "address_zip")
    val addressZip: String? = null,

    @Json(name = "address_country")
    val addressCountry: String? = null,

    val notes: String? = null,
    val reference: String? = null,

    @Json(name = "allow_invoice_credit_card_payments")
    val allowInvoiceCreditCardPayments: Boolean? = null,

    @Json(name = "has_address")
    val hasAddress: Boolean? = null,

    @Json(name = "parent_merge")
    val parentMerge: String? = null,

    @Json(name = "child_merges")
    val childMerges: List<String>? = null,

    @Json(name = "created_at")
    @Utc
    val createdAt: Date? = null,

    @Json(name = "updated_at")
    @Utc
    val updatedAt: Date? = null
) {
    fun updating(): Update = Update(this)

    data class Update(
        private val customer: StaxCustomer,
        private val changes: MutableMap<String, Any?> = mutableMapOf()
    ) {
        operator fun set(field: String, value: Any?) {
            changes[field] = value
        }

        operator fun get(field: String): Any? {
            return changes[field] ?: getOriginal(field)
        }

        private fun getOriginal(field: String): Any? {
            return when (field) {
                "firstname" -> customer.firstname
                "lastname" -> customer.lastname
                "company" -> customer.company
                "email" -> customer.email
                "ccEmails" -> customer.ccEmails
                "ccSms" -> customer.ccSms
                "phone" -> customer.phone
                "address1" -> customer.address1
                "address2" -> customer.address2
                "addressCity" -> customer.addressCity
                "addressState" -> customer.addressState
                "addressZip" -> customer.addressZip
                "addressCountry" -> customer.addressCountry
                "notes" -> customer.notes
                "reference" -> customer.reference
                "allowInvoiceCreditCardPayments" -> customer.allowInvoiceCreditCardPayments
                else -> null
            }
        }

        fun apply(): StaxCustomer {
            return customer.copy(
                firstname = changes["firstname"] as? String ?: customer.firstname,
                lastname = changes["lastname"] as? String ?: customer.lastname,
                company = changes["company"] as? String ?: customer.company,
                email = changes["email"] as? String ?: customer.email,
                ccEmails = changes["ccEmails"] as? List<String> ?: customer.ccEmails,
                ccSms = changes["ccSms"] as? List<String> ?: customer.ccSms,
                phone = changes["phone"] as? String ?: customer.phone,
                address1 = changes["address1"] as? String ?: customer.address1,
                address2 = changes["address2"] as? String ?: customer.address2,
                addressCity = changes["addressCity"] as? String ?: customer.addressCity,
                addressState = changes["addressState"] as? String ?: customer.addressState,
                addressZip = changes["addressZip"] as? String ?: customer.addressZip,
                addressCountry = changes["addressCountry"] as? String ?: customer.addressCountry,
                notes = changes["notes"] as? String ?: customer.notes,
                reference = changes["reference"] as? String ?: customer.reference,
                allowInvoiceCreditCardPayments = changes["allowInvoiceCreditCardPayments"] as? Boolean ?: customer.allowInvoiceCreditCardPayments,
                hasAddress = customer.hasAddress,
                parentMerge = customer.parentMerge,
                childMerges = customer.childMerges,
                createdAt = customer.createdAt,
                updatedAt = customer.updatedAt
            )
        }
    }
}
