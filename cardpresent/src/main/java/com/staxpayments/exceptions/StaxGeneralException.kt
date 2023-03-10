package com.staxpayments.exceptions

class StaxGeneralException(detail: String) : StaxException("Stax General Error", detail) {
    companion object {
        val unknown = StaxGeneralException("Unknown error has occurred")
        val uninitialized = StaxGeneralException("Stax SDK has not been initialized yet")
    }
}