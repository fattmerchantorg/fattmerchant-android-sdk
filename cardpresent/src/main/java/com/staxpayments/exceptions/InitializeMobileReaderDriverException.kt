package com.staxpayments.exceptions

class InitializeMobileReaderDriverException(message: String? = null) :
    StaxException("Could not initialize mobile reader driver", message)