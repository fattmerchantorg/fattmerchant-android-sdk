package com.fattmerchant.omni

import com.fattmerchant.omni.data.UserNotification

/**
 * Listens to user notification updates
 *
 * Fired when a prompt is required to be shown to the customer.
 *
 * @see UserNotification
 */
interface UserNotificationListener {
    /**
     * Called when a prompt is required to be shown to a customer and returns a [UserNotification]
     * @see UserNotification
     */
    fun onUserNotification(userNotification: UserNotification)

    /**
     * Called when a prompt is required to be shown to a customer and returns the raw value from ChipDNA
     *
     * Possible values include:
     *
     * *ReferToDevice* - Indicates referral to the device.
     *
     * *ChipReadErrorApplicationNotSupportedPleaseRetry* - Indicates there has been an issue with the ICC chip and the user should retry.
     *
     * *PresentOneCardOnly* - Indicates that only one card should be presented.
     *
     * *FallbackSwipeCard* - Indicates that fallback to Swipe has occurred.
     *
     * *FallforwardSwipeCard* - Indicates that fallforward from Contactless to Swipe has occurred.
     *
     * *FallforwardInsertCard* - Indicates that fallforward from Contactless to Insert has occurred.
     *
     * *FallforwardInsertSwipeCard* - Indicates that fallforward from Contactless to Insert/Swipe has occurred.
     *
     * *TryCardAgain* - Indicates that the card should be tried again.
     */
    fun onRawUserNotification(userNotification: String)
}