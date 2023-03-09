package com.staxpayments.android.anywherecommerce

import com.anywherecommerce.android.sdk.MeaningfulMessage
import com.anywherecommerce.android.sdk.devices.CardReader
import com.anywherecommerce.android.sdk.endpoints.AnyPayTransaction
import com.anywherecommerce.android.sdk.models.TransactionStatus
import com.anywherecommerce.android.sdk.models.TransactionType
import com.staxpayments.android.chipdna.ConnectionType
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.TransactionResult
import com.staxpayments.sdk.data.TransactionUpdate
import com.staxpayments.sdk.data.models.Transaction
import com.anywherecommerce.android.sdk.util.Amount as ANPAmount

internal fun CardReader.toMobileReader(): MobileReader {
    return object : MobileReader {
        override fun getName(): String = serialNumber
        override fun getFirmwareVersion(): String? = firmwareVersion
        override fun getMake(): String? = null
        override fun getModel(): String? = modelDisplayName
        override fun serialNumber(): String? = serialNumber
        override fun getConnectionType(): ConnectionType = ConnectionType.UNKNOWN
    }
}

internal fun TransactionUpdate.Companion.from(anpMeaningfulMessage: MeaningfulMessage): TransactionUpdate? {

    return anpMeaningfulMessage.toString().let { message ->
        when (message) {
            "SWIPE OR INSERT OR TAP", "Insert, Swipe, or Tap Card" -> PromptInsertSwipeTap
            "SWIPE OR INSERT" -> PromptInsertSwipeCard
            "PROCESSING" -> Authorizing
            "REMOVE_CARD" -> PromptRemoveCard
            "INSERT_CARD" -> PromptInsertCard
            else -> null
        }
    }
}

internal fun Amount.Companion.from(anyPayAmount: ANPAmount): Amount {
    return Amount(anyPayAmount.toDecimal().multiply(100.0.toBigDecimal()).toInt())
}

/**
 * For card type, AWC gives us a string like "Visa Credit". This function takes that and returns
 * "visa", which is what Omni expects
 *
 * @param cardType
 * @return the actual card type
 */
fun mapCardType(cardType: String): String {
    return if (cardType.contains("visa", true)) {
        "visa"
    } else if (cardType.contains("mastercard", true)) {
        "mastercard"
    } else if (cardType.contains("american express", true) || cardType.contains("amex", true)) {
        "amex"
    } else if (cardType.contains("discover", true)) {
        "discover"
    } else {
        cardType
    }
}

internal fun TransactionResult.Companion.from(transaction: AnyPayTransaction): TransactionResult = TransactionResult().apply {
    if (transaction.status == TransactionStatus.DECLINED) {
        // Add the responseText, since it usually contains info about why the transaction declined
        message = transaction.responseText
    }

    source = AWCDriver().source
    success = transaction.status == TransactionStatus.APPROVED

    val expiryMonth = transaction.cardExpiryMonth ?: null
    val expiryYear = transaction.cardExpiryYear ?: null

    if (expiryMonth != null && expiryYear != null) {
        cardExpiration = "$expiryMonth$expiryYear"
    }

    transaction.approvedAmount?.let { amount = Amount.from(it) }
    transaction.refTransactionId?.let { externalId = it }

    if (externalId == null) {
        transaction.externalId?.let { externalId = it }
    }

    transaction.maskedPAN?.let { maskedPan = it }
    transaction.cardType?.let { cardType = mapCardType(it) }
    transaction.approvalCode?.let { authCode = it}
    transaction.cardholderName?.let {
        val name = it.split(" ").toMutableList()
        cardHolderFirstName = name.removeAt(0)
        cardHolderLastName = name.joinToString(" ")
    }
    transactionType = when (transaction.transactionType) {
        TransactionType.REFUND -> "refund"
        TransactionType.VOID -> "void"
        else -> "sale"
    }
}

internal fun Transaction.awcExternalId(): String? {
    return (meta as? Map<*, *>)?.get("awcTransactionId") as? String
}
