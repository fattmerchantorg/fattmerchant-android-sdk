package com.fattmerchant.omni.data

/**
 * Represents a user notification
 *
 * This object will provide information about events that prompt the user.
 */
open class UserNotification(var value: String, val userFriendlyMessage: String? = null) {
    companion object {
        /** Indicates referral to the device */
        val ReferToDevice = UserNotification("Prompt User Refer To Device", "Please check your device.")

        /** Indicates there has been an issue with the ICC chip and the user should retry */
        val ChipReadErrorApplicationNotSupportedPleaseRetry = UserNotification("Prompt User Error Application Not Supported",
                "There was an error with processing the chip card. Please try again.")

        /** Indicates that only one card should be presented */
        val PresentOneCardOnly = UserNotification("Prompt User Present One Card Only", "Please only present one card.")

        /** Indicates that fallback to Swipe has occurred */
        val FallbackSwipeCard = UserNotification("Prompt User Fallback Swipe Card", "Please swipe your card.")

        /** Indicates that fallforward from Contactless to Swipe has occurred */
        val FallforwardSwipeCard = UserNotification("Prompt User Fallforward Swipe Card", "Please swipe your card.")

        /** Indicates that fallforward from Contactless to Insert has occurred */
        val FallforwardInsertCard = UserNotification("Prompt User Fallback Insert Card", "Please insert your card.")

        /** Indicates that fallforward from Contactless to Insert/Swipe has occurred */
        val FallforwardInsertSwipeCard = UserNotification("Prompt User Fallforward Insert Swipe Card", "Please insert or swipe your card.")

        /** Indicates that the card should be tried again */
        val TryCardAgain = UserNotification( "Prompt User Try Card Again","Please try your card again.")
    }
}