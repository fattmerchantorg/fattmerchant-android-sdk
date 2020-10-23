package com.fattmerchant.omni

import com.fattmerchant.omni.data.models.MobileReaderConnectionStatus

interface MobileReaderConnectionStatusListener {

    /**
     * Called when [MobileReader] has a new [MobileReaderConnectionStatus]
     *
     * @param status the new [MobileReaderConnectionStatus] of the [MobileReader]
     */
    fun mobileReaderConnectionStatusUpdate(status: MobileReaderConnectionStatus)
}