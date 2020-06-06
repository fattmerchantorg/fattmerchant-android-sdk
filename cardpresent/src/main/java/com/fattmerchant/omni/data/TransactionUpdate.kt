package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.models.Transaction

/**
 * Represents an update in the transaction
 *
 * This object will provide information about events with the transaction. Some examples of events
 * are "Transaction Started" or "Signature Provided"
 */
final class TransactionUpdate(val value: String, val userFriendlyMessage: String? = null) {

    companion object {
        /** Request card be swiped or inserted */
        val PromptInsertSwipeCard = TransactionUpdate("Prompt Insert Swipe Card", "Please insert or swipe card")

        /** Request card be swiped */
        val PromptSwipeCard = TransactionUpdate("Prompt Swipe Card", "Please swipe card")

        /** Card was swiped */
        val CardSwiped = TransactionUpdate("Card Swiped")

        /** Card was inserted */
        val CardInserted = TransactionUpdate("Card Inserted")

        /** Card Swipe error */
        val CardSwipeError = TransactionUpdate("Card Swipe Error", "Card swipe error. Please try again")

        /** Request card be removed */
        val PromptRemoveCard = TransactionUpdate("Prompt Remove Card", "Please remove card")

        /** Card was removed */
        val CardRemoved = TransactionUpdate("Card Removed")

        /** Prompt provide signature */
        val PromptProvideSignature = TransactionUpdate("Request Signature", "Please provide signature")

        /** Signature provided */
        val SignatureProvided = TransactionUpdate("Signature Provided")
    }
}