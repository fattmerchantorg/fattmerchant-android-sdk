package com.fattmerchant.cpresent.android

import android.content.Context

import com.fattmerchant.cpresent.omni.entity.OmniClient
import com.fattmerchant.cpresent.omni.entity.models.Invoice
import com.fattmerchant.cpresent.omni.entity.models.Transaction
import com.fattmerchant.cpresent.omni.entity.repository.CustomerRepository
import com.fattmerchant.cpresent.omni.entity.repository.InvoiceRepository
import com.fattmerchant.cpresent.omni.entity.repository.PaymentMethodRepository
import com.fattmerchant.cpresent.omni.entity.repository.TransactionRepository
import com.fattmerchant.cpresent.omni.networking.OmniApi
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import java.util.logging.Logger

class Omni(override var omniApi: OmniApi) : OmniClient, CoroutineScope {

    /**
     * Thrown when Omni failed to initialize
     *
     * @param message describes what went wrong
     * */
    class InitializationError(message: String? = null): Exception(message)

    override var transactionRepository = object: TransactionRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    override var invoiceRepository = object: InvoiceRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    override var customerRepository = object: CustomerRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    override var paymentMethodRepository = object: PaymentMethodRepository {
        override var omniApi: OmniApi = this@Omni.omniApi
    }

    override var mobileReaderDriverRepository = MobileReaderDriverRepository()

    private val job = SupervisorJob()

    override var coroutineContext = Dispatchers.Default + job

    companion object {
        private val log: Logger = Logger.getLogger("OmniService")
        private var sharedInstance: Omni? = null

        private fun log(msg: String?) {
            log.info("[${Thread.currentThread().name}] $msg")
        }

        /**
         * Prepares [Omni] for usage
         *
         * @param context Application Context
         * @param params a [Map] containing the necessary information to initialize Omni
         * @param completion block to execute once completed
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(context: Context, params: Map<String, Any>, completion: () -> Unit, error: (Error) -> Unit) {
            if (!params.containsKey("apiKey")) {
                throw InitializationError("Must pass an apiKey to the initialize call")
            }

            // Get the params
            val initParams = params.toMutableMap()
            initParams["appContext"] = context

            // Init the API
            val omniApi = OmniApi()
            omniApi.token = initParams["apiKey"] as String

            // Create the shared Omni object
            val omni = Omni(omniApi)
            sharedInstance = omni

            // Init omni
            omni.launch {

                var failed = false
                omni.initialize(initParams) { error ->
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
