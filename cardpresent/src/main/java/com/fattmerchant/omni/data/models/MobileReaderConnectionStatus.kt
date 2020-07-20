package com.fattmerchant.omni.data.models

/** The status of a {MobileReader} */
class MobileReaderConnectionStatus {

    companion object {
        /** The reader has been found by the Android device and is currently being connected */
        val connecting: String = "connecting"

        /** The reader is connected */
        val connected: String = ""

        /** The reader is disconnected */
        val disconnected: String = ""

        /** The reader is performing an update
         * - Note: This might be a long-running operation
         */
        val updating: String = ""

        /** The reader is performing a reboot */
        val rebooting: String = ""
    }

}