package com.staxpayments.exceptions

class DisconnectMobileReaderException(detail: String) : StaxException("Could not disconnect mobile reader", detail) {
    companion object {
        val driverNotFound = DisconnectMobileReaderException("Driver not found")
    }
}