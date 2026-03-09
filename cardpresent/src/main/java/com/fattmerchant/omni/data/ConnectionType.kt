package com.fattmerchant.omni.data

/**
 * Represents the connection type of a mobile reader device.
 */
enum class ConnectionType {
    BT,
    BLE,
    USB,
    NFC,
    UNKNOWN;

    companion object {
        // String constants matching ChipDNA ParameterValues for connection types
        const val BLUETOOTH_CONNECTION_TYPE = "BluetoothConnectionType"
        const val BLUETOOTH_LE_CONNECTION_TYPE = "BluetoothLeConnectionType"
        const val USB_CONNECTION_TYPE = "UsbConnectionType"

        fun parse(str: String): ConnectionType {
            return if (str.equals(BLUETOOTH_CONNECTION_TYPE, ignoreCase = true)) { BT }
            else if (str.equals(BLUETOOTH_LE_CONNECTION_TYPE, ignoreCase = true)) { BLE }
            else if (str.equals(USB_CONNECTION_TYPE, ignoreCase = true)) { USB }
            else if (str.equals("NFC", ignoreCase = true)) { NFC }
            else { UNKNOWN }
        }
    }

    fun toParameterValue(): String {
        return when (this) {
            BT -> BLUETOOTH_CONNECTION_TYPE
            BLE -> BLUETOOTH_LE_CONNECTION_TYPE
            USB -> USB_CONNECTION_TYPE
            else -> BLUETOOTH_LE_CONNECTION_TYPE // Default to BLE
        }
    }
}
