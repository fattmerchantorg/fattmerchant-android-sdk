package com.staxpayments.sdk.data.models

/** The status of a {MobileReader} */
enum class MobileReaderConnectionStatus(val status: String) {
    /** The reader has been found by the Android device and is currently being connected */
    CONNECTING("connecting"),

    /** The reader is connected */
    CONNECTED("connected"),

    /** The reader is disconnected */
    DISCONNECTED("disconnected"),

    /** The reader is performing an update
     * - Note: This includes TMS and Config updates
     */
    UPDATING_CONFIGURATION("updatingConfiguration"),

    /** The reader is performing an update on the firmware
     * - Note: This is a long running operation
     */
    UPDATING_FIRMWARE("updatingFirmware"),

    /** The reader is performing a reboot */
    REBOOTING("rebooting");

    companion object { }
}
