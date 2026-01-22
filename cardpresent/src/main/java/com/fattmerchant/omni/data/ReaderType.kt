package com.fattmerchant.omni.data

/**
 * Specifies which reader type should be used for a transaction.
 */
enum class ReaderType {
    /**
     * Use the device's NFC (Tap to Pay) for the transaction.
     */
    TAP_TO_PAY,
    
    /**
     * Use an external reader (Bluetooth/USB) for the transaction.
     */
    EXTERNAL_READER,
    
    /**
     * Let the SDK automatically select based on which reader is connected.
     * - If only Tap to Pay reader is connected, uses Tap to Pay
     * - If external reader is connected, uses external reader
     * - Prefers external reader if both are connected
     */
    AUTO
}
