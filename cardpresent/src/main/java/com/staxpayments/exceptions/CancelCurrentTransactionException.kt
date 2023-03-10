package com.staxpayments.exceptions

class CancelCurrentTransactionException(message: String? = null) : StaxException("", message) {
    companion object {
        val NoTransactionToCancel = CancelCurrentTransactionException("There is no transaction to cancel")
        val Unknown = CancelCurrentTransactionException("Unknown error")
    }
}