package com.staxpayments.android

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.CommonStax
import com.staxpayments.sdk.Environment
import com.staxpayments.api.StaxApi
import kotlinx.coroutines.launch
import com.staxpayments.api.repository.MobileReaderDriverRepository as CommonMobileReaderDriverRepo

/**
 * Communicates with the Stax platform and bluetooth mobile readers
 *
 * ## Usage
 * ```
 * // First, initialize Stax using InitParams
 * val initParams = InitParams(myAppContext, apiKey)
 * Stax.initialize(initParams, {
 *   // Stax is now initialized and ready!
 *
 * }, { staxException ->
 *
 * )
 * ```
 *
 * Once initialized, you can use methods like `Stax.shared().getAvailableReaders`
 *
 * @see InitParams
 * @property staxApi
 */
class Stax internal constructor(staxApi: StaxApi) : CommonStax(staxApi) {

    /**
     * Thrown when Stax failed to initialize
     *
     * @param message describes what went wrong
     * */
    class InitializationError(message: String? = null) : Exception(message)

    override var mobileReaderDriverRepository: CommonMobileReaderDriverRepo = MobileReaderDriverRepository()

    companion object {

        private var sharedInstance: Stax? = null

        /**
         * Prepares [Stax] for usage
         *
         * @param params an [InitParams] instance that has all the necessary info for initialization
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(params: InitParams, completion: () -> Unit, error: (StaxException) -> Unit) {
            val paramMap = mutableMapOf(
                "apiKey" to params.apiKey,
                "appContext" to params.appContext,
                "appId" to params.appId
            )

            params.application?.let {
                paramMap["application"] = it
            }

            initialize(paramMap, params.environment, completion, error)
        }

        /**
         * Prepares [Stax] for usage
         *
         * @param params a [Map] containing the necessary information to initialize Stax
         * @param completion block to execute once completed
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(params: Map<String, Any>, environment: Environment, completion: () -> Unit, error: (StaxException) -> Unit) {
            // Init the API
            val staxApi = StaxApi()

            staxApi.environment = environment

            staxApi.token = params["apiKey"] as? String ?: ""

            // Create the shared Stax object
            val stax = Stax(staxApi)
            sharedInstance = stax

            // Init com.staxpayments.sdk
            stax.coroutineScope.launch {
                try {
                    stax.initialize(params, {
                        completion()
                    }) {
                        error(it)
                    }
                }catch (e: Exception){
                    error(e)
                }

            }
        }

        fun shared(): Stax? = sharedInstance
    }
}
