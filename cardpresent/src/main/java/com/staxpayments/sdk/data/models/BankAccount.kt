package com.staxpayments.sdk.data.models

import com.squareup.moshi.Json

/** A bank account */
class BankAccount(

    @Json(name = "person_name")
    var personName: String,

    @Json(name = "bank_type")
    var bankType: String = "checkings",

    @Json(name = "bank_holder_type")
    var bankHolderType: String = "business",

    @Json(name = "bank_account")
    var bankAccount: String,

    @Json(name = "bank_routing")
    var bankRouting: String,

    @Json(name = "address_zip")
    var addressZip: String,

    @Json(name = "bank_name")
    var bankName: String? = null,

    @Json(name = "address_1")
    var address1: String? = null,

    @Json(name = "address_2")
    var address2: String? = null,

    @Json(name = "address_city")
    var addressCity: String? = null,

    @Json(name = "address_state")
    var addressState: String? = null,

    @Json(name = "customer_id")
    var customerId: String? = null,

    var note: String? = null,
    var phone: String? = null,
    var email: String? = null
) {

    private var method: String = "bank"

    @Deprecated(
        "Please use the new constructor that does not use the `method` parameter",
        ReplaceWith("BankAccount(personName, bankType, bankHolderType, bankAccount, bankRouting, addressZip, bankName, address1, address2, addressCity, addressState, customerId, note, phone, email)")
    )
    constructor(
        personName: String,
        bankType: String,
        bankHolderType: String,
        bankAccount: String,
        bankRouting: String,
        addressZip: String,
        bankName: String?,
        address1: String?,
        address2: String?,
        addressCity: String?,
        addressState: String?,
        customerId: String?,
        note: String?,
        phone: String?,
        email: String?,
        method: String?
    ) : this(personName, bankType, bankHolderType, bankAccount, bankRouting, addressZip, bankName, address1, address2, addressCity, addressState, customerId, note, phone, email)

    fun firstName(): String {
        val splitName = personName.split(" ")
        return splitName.first()
    }

    fun lastName(): String {
        val splitName = personName.split(" ") as MutableList<String>
        splitName.removeAt(0)
        return splitName.joinToString(" ")
    }

    companion object {
        /**
         * Creates a test bank account
         *
         * @return a test bank account
         */
        fun testBankAccount() = BankAccount(personName = "Jim Parsnip", bankType = "savings", bankAccount = "9876543210", bankRouting = "021000021", addressZip = "32822").apply {
            address1 = "123 Orange Ave"
            address2 = "Unit 309"
            addressCity = "Orlando"
            addressState = "FL"
            phone = "3210000000"
            email = "fatt@merchant.com"
            note = "This is a test credit card"
        }

        /**
         * Creates a test bank account that fails processing
         *
         * @return a test bank account
         */
        fun failingTestBankAccount() = BankAccount(personName = "Jim Parsnip", bankAccount = "9876543210", bankRouting = "021000021", addressZip = "32822").apply {
            address1 = "123 Orange Ave"
            address2 = "Unit 309"
            addressCity = "Orlando"
            addressState = "FL"
            phone = "3210000000"
            email = "fatt@merchant.com"
            note = "This is a test credit card"
        }
    }
}
