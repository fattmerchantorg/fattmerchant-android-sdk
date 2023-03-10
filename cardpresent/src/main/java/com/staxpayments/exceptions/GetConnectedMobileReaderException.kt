package com.staxpayments.exceptions

class GetConnectedMobileReaderException(detail: String) : StaxException("Could not get connected mobile reader", detail) {
    companion object {
        val noReaderAvailable = GetConnectedMobileReaderException("No mobile reader is available")
    }
}