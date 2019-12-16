package omni.data.models

import kotlin.Exception

/**
 * An Exception that happened with regard to omni
 *
 * @property message A pretty message that can be shown to a user
 * @property detail The cause of the exception
 */
open class OmniException(override var message: String, open var detail: String? = null) : Exception()