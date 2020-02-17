package com.fattmerchant.android.chipdna

import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.ParameterValues
import com.creditcall.chipdnamobile.Parameters
import com.fattmerchant.android.chipdna.ChipDnaDriver
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Makes an instance of [MobileReader] for the given [pinPad]
 *
 * @param pinPad
 * @return
 */
fun mapPinPadToMobileReader(pinPad: ChipDnaDriver.SelectablePinPad): MobileReader {
    return object : MobileReader {
        override fun getName() = pinPad.name
    }
}

/**
 * Tries to get the parameter by key.
 *
 * @return null if not found
 */
operator fun Parameters?.get(key: String): String? = this?.getValue(key)

/**
 * The param value to make NMI add a customer to the customer vault
 *
 * This should be used with the ParameterKeys.CustomerVaultCommand and passed into startTransaction()
 */
val ParameterValuesAddCustomer = "add-customer"

/**
 * Gets the User Reference from the given [Transaction]
 *
 * @param transaction
 * @return a string containing the user reference or null if not found
 */
internal fun extractUserReference(transaction: Transaction): String? =
        (transaction.meta as? Map<*, *>)?.get("nmiUserRef") as? String

/**
 * Generates a user reference for chipDNA transactions
 *
 * @return String containing the generated user reference
 */
internal fun generateUserReference(): String =
        String.format("CDM-%s", SimpleDateFormat("yy-MM-dd-HH.mm.ss", Locale.US).format(Date()))

internal fun Parameters.withTransactionRequest(request: TransactionRequest) = Parameters().apply {
    add(ParameterKeys.Amount, request.amount.centsString())
    add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual)
    add(ParameterKeys.Currency, "USD")
    add(ParameterKeys.UserReference, generateUserReference())
    add(ParameterKeys.PaymentMethod, ParameterValues.Card)
    add(ParameterKeys.AutoConfirm, ParameterValues.TRUE)
    add(ParameterKeys.TransactionType, ParameterValues.Sale)

    if (request.tokenize) {
        add(ParameterKeys.CustomerVaultCommand, ParameterValuesAddCustomer)
    }
}