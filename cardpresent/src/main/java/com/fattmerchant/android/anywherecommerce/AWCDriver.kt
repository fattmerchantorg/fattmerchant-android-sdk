package com.fattmerchant.android.anywherecommerce

import android.app.Application
import android.content.Context
import com.anywherecommerce.android.sdk.AuthenticationListener
import com.anywherecommerce.android.sdk.MeaningfulError
import com.anywherecommerce.android.sdk.SDKManager
import com.anywherecommerce.android.sdk.Terminal
import com.anywherecommerce.android.sdk.endpoints.anywherecommerce.AnywhereCommerce
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetEndpoint
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.Transaction

import com.fattmerchant.omni.data.MobileReaderDriver.*
import com.fattmerchant.omni.data.models.Merchant
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AWCDriver: MobileReaderDriver {

    /** The endpoint that AnywhereCommerce will be reaching out to */
    private var endpoint: WorldnetEndpoint? = null

    /** The URL of the gateway that AnywhereCommerce will be reaching out to */
    private var gatewayUrl = "https://payments.anywherecommerce.com/merchant"

    override suspend fun isReadyToTakePayment(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun initialize(args: Map<String, Any>): Boolean {
        // Make sure we have all the necessary data
        val application = args["application"] as? Application
                ?: throw InitializeMobileReaderDriverException("appContext not found")

        val merchant = args["merchant"] as? Merchant
                ?: throw InitializeMobileReaderDriverException("merchant not found")

        val emvTerminalId = merchant.emvTerminalId()
                ?: throw InitializeMobileReaderDriverException("emvTerminalId not found")

        val emvTerminalSecret = merchant.emvTerminalSecret()
                ?: throw InitializeMobileReaderDriverException("emvTerminalSecret not found")

        // Initialize the Terminal. This will allow us to interact with AnyPay later on
        SDKManager.initialize(application)
        Terminal.initialize()

        // Create the endpoint
        val endpoint = Terminal.instance.endpoint as? WorldnetEndpoint
                ?: throw InitializeMobileReaderDriverException("Could not create worldnet endpoint")

        endpoint.worldnetTerminalID = emvTerminalId
        endpoint.worldnetSecret = emvTerminalSecret
        endpoint.gatewayUrl = gatewayUrl


        // Authenticate
        return suspendCancellableCoroutine {
            endpoint.authenticate(object: AuthenticationListener {
                override fun onAuthenticationComplete() {
                    this@AWCDriver.endpoint = endpoint
                    it.resume(true)
                }

                override fun onAuthenticationFailed(p0: MeaningfulError?) {
                    this@AWCDriver.endpoint = null
                    it.resume(false)
                }
            })
        }

    }

    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        TODO("Not yet implemented")
    }

    override suspend fun connectReader(reader: MobileReader): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun performTransaction(request: TransactionRequest): TransactionResult {
        TODO("Not yet implemented")
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        TODO("Not yet implemented")
    }

    override suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount?): TransactionResult {
        TODO("Not yet implemented")
    }

}