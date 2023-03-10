package com.staxpayments.exceptions

class ChipDnaConnectReaderException(message: String? = null) :
    ConnectReaderException(mapDetailMessage(message)) {
    companion object {
        fun mapDetailMessage(chipDnaMessage: String?): String? {
            return when (chipDnaMessage) {
                "ConnectionClosed" -> "Connection closed"
                "BluetoothNotEnabled" -> "Bluetooth not enabled"
                else -> chipDnaMessage
            }
        }
    }
}