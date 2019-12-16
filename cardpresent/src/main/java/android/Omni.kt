package android

import omni.data.models.OmniException
import omni.Omni as CommonOmni
import omni.data.repository.MobileReaderDriverRepository as CommonMobileReaderDriverRepo
import omni.networking.OmniApi
import kotlinx.coroutines.launch

class Omni(omniApi: OmniApi) : CommonOmni(omniApi) {

    /**
     * Thrown when Omni failed to initialize
     *
     * @param message describes what went wrong
     * */
    class InitializationError(message: String? = null) : Exception(message)

    override var mobileReaderDriverRepository: CommonMobileReaderDriverRepo = MobileReaderDriverRepository()

    companion object {

        private var sharedInstance: Omni? = null

        /**
         * Prepares [Omni] for usage
         *
         * @param params an [InitParams] instance that has all the necessary info for initialization
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(params: InitParams, completion: () -> Unit, error: (OmniException) -> Unit) {
            val paramMap = mapOf (
                "apiKey" to params.apiKey,
                "context" to params.appContext
            )

            initialize(paramMap, completion, error)
        }

        /**
         * Prepares [Omni] for usage
         *
         * @param params a [Map] containing the necessary information to initialize Omni
         * @param completion block to execute once completed
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(params: Map<String, Any>, completion: () -> Unit, error: (OmniException) -> Unit) {
            // Init the API
            val omniApi = OmniApi()
            omniApi.environment = OmniApi.Environment.LIVE
            omniApi.token = params["apiKey"] as? String ?: ""

            // Create the shared Omni object
            val omni = Omni(omniApi)
            sharedInstance = omni

            // Init omni
            omni.coroutineScope.launch {

                var failed = false
                omni.initialize(params) { error ->
                    failed = true
                    error(error)
                }.invokeOnCompletion {
                    if (!failed) {
                        completion()
                    }
                }

            }
        }

        fun shared(): Omni? = sharedInstance
    }

}
