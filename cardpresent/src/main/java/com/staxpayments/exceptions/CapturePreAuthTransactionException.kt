package com.staxpayments.exceptions

class CapturePreAuthTransactionException(detail: String) : StaxException("Could not get connected mobile reader", detail)
