package com.fattmerchant.cpresent.android.chipdna

import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.ParameterValues
import com.creditcall.chipdnamobile.Parameters
import com.fattmerchant.cpresent.omni.entity.MobileReader
import com.fattmerchant.cpresent.omni.entity.TransactionRequest
import java.text.SimpleDateFormat
import java.util.*

fun mapPinPadToMobileReader(pinPad: ChipDnaDriver.SelectablePinPad): MobileReader {
    return object : MobileReader {
        override fun getName() = pinPad.name
    }
}

fun mapTransactionRequestToParams(request: TransactionRequest) = Parameters().apply {
    val userRef = String.format("CDM-%s", SimpleDateFormat("yy-MM-dd-HH.mm.ss", Locale.US).format(Date()))
    add(ParameterKeys.Amount, request.amount.centsString())
    add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual)
    add(ParameterKeys.Currency, "USD")
    add(ParameterKeys.UserReference, userRef)
    add(ParameterKeys.TransactionType, ParameterValues.Sale)
    add(ParameterKeys.PaymentMethod, ParameterValues.Card)
}

/**
 * Tries to get the parameter by key.
 *
 * @return null if not found
 */
operator fun Parameters?.get(key: String): String? = this?.getValue(key)