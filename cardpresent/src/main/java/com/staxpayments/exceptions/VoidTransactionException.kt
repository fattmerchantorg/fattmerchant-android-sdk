package com.staxpayments.exceptions

class VoidTransactionException(message: String? = null) :
    StaxException("Could not void transaction", message)