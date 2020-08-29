package com.fattmerchant.omni.data.models

/** The status of a {MobileReader} */
enum class MobileReaderConnectionStatus(val status: String) {
    /** The reader has been found by the Android device and is currently being connected */
    CONNECTING("connecting"),

    /** The reader is connected */
    CONNECTED("connected"),

    /** The reader is disconnected */
    DISCONNECTED("disconnected"),

    /** The reader is performing an update
     * - Note: This might be a long-running operation
     */
    UPDATING("updating"),

    /** The reader is performing a reboot */
    REBOOTING("rebooting");

    companion object { }
}