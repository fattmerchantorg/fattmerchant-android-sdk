package com.staxpayments.exceptions

import kotlin.Exception

/**
 * An Exception that happened with regard to com.fattmerchant.Stax
 *
 * @property message A pretty message that can be shown to a user
 * @property detail The cause of the exception
 */
open class StaxException(override var message: String, open var detail: String? = null) : Exception()
