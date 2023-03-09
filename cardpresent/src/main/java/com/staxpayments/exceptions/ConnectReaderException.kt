package com.staxpayments.exceptions

open class ConnectReaderException(message: String? = null) :
    StaxException("Could not connect mobile reader", message)