package com.staxpayments.exceptions

class PerformTransactionException(message: String? = null) :
    StaxException("Could not perform transaction", message)