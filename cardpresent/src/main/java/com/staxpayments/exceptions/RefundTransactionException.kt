package com.staxpayments.exceptions

class RefundTransactionException(message: String? = null) :
    StaxException("Could not refund transaction", message)