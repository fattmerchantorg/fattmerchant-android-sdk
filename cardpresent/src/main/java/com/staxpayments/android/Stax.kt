package com.staxpayments.android

import android.app.Application
import android.content.Context
import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.CommonStax
import com.staxpayments.sdk.Environment
import com.staxpayments.api.StaxApi
import com.staxpayments.exceptions.StaxGeneralException
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
         * Initializes the [Stax] SDK for usage
         * @param context the Android application Context
         * @param application your custom Android Application class
         * @param apiKey the Stax API Key
         * @param onCompletion the completion callback on successful initialization
         * @param onError the error callback on a failed initialization
         * @param environment An optional parameter for testing against the developer API. If unsure, leave this blank
         * @param appId An optional parameter for testing against the developer API. If unsure, leave this blank
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(
            context: Context,
            application: Application,
            apiKey: String,
            onCompletion: () -> Unit,
            onError: (StaxException) -> Unit,
            appId: String = "appid",
            environment: Environment = Environment.LIVE,
        ) {
            val paramMap = mutableMapOf(
                "apiKey" to apiKey,
                "appContext" to context,
                "appId" to appId,
                "application" to application
            )

            initialize(paramMap, environment, onCompletion, onError)
        }

        /**
         * Prepares [Stax] for usage
         * @param params an [InitParams] instance that has all the necessary info for initialization
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(params: InitParams, onCompletion: () -> Unit, onError: (StaxException) -> Unit) {
            val paramMap = mutableMapOf(
                "apiKey" to params.apiKey,
                "appContext" to params.appContext,
                "appId" to params.appId
            )

            params.application?.let {
                paramMap["application"] = it
            }

            initialize(paramMap, params.environment, onCompletion, onError)
        }

        /**
         * Prepares [Stax] for usage
         *
         * @param params a [Map] containing the necessary information to initialize Stax
         * @param onCompletion block to execute once completed
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        private fun initialize(params: Map<String, Any>, environment: Environment, onCompletion: () -> Unit, onError: (StaxException) -> Unit) {
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
                        onCompletion()
                    }) {
                        onError(it)
                    }
                } catch (e: StaxException){
                    onError(e)
                } catch (e: Exception) {
                    error(e)
                }
            }
        }

        /**
         * Get the shared instance of the Stax SDK
         * @throws StaxGeneralException A Stax Exception if your SDK has not been initialized
         */
        fun instance(): Stax {
            return sharedInstance ?: throw StaxGeneralException.uninitialized
        }
    }
}
