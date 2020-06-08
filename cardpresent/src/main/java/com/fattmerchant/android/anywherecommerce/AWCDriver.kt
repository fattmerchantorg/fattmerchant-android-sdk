package com.fattmerchant.android.anywherecommerce

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.anywherecommerce.android.sdk.AuthenticationListener
import com.anywherecommerce.android.sdk.MeaningfulError
import com.anywherecommerce.android.sdk.SDKManager
import com.anywherecommerce.android.sdk.Terminal
import com.anywherecommerce.android.sdk.devices.*
import com.anywherecommerce.android.sdk.endpoints.anywherecommerce.AnywhereCommerce
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetEndpoint
import com.anywherecommerce.android.sdk.services.CardReaderConnectionService
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.Transaction

import com.fattmerchant.omni.data.MobileReaderDriver.*
import com.fattmerchant.omni.data.models.Merchant
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class AWCDriver: MobileReaderDriver {

    /** The endpoint that AnywhereCommerce will be reaching out to */
    private var endpoint: WorldnetEndpoint? = null

    /** The URL of the gateway that AnywhereCommerce will be reaching out to */
    private var gatewayUrl = "https://payments.anywherecommerce.com/merchant"

    override var familiarSerialNumbers: MutableList<String> = mutableListOf()

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

    override suspend fun isInitialized(): Boolean = Terminal.isInitialized()

    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        return suspendCancellableCoroutine {
            CardReader.connect(CardReader.ConnectionMethod.BLUETOOTH, object : CardReaderConnectionListener<CardReader>, MultipleBluetoothDevicesFoundListener {
                override fun onCardReaderConnectionFailed(p0: MeaningfulError?) {
                    it.resume(listOf())
                }

                override fun onCardReaderConnected(connectedReader: CardReader?) {
                    val list = mutableListOf<MobileReader>()
                    connectedReader?.let { reader ->
                        val mobileReader = reader.toMobileReader()

                        // Add the serial number to the list of familiar ones. This helps
                        // with recognizing that this reader belongs to this driver
                        mobileReader.serialNumber()?.let { serial ->
                            familiarSerialNumbers.add(serial)
                        }

                        list.add(mobileReader)
                    }
                    it.resume(list)
                }

                override fun onMultipleBluetoothDevicesFound(p0: MutableList<BluetoothDevice>?) {
                    it.resume(listOf())
                }
            })
        }
    }

    override suspend fun connectReader(reader: MobileReader): Boolean {
        return (CardReaderController.isCardReaderConnected()
                && CardReaderController.getConnectedReader().serialNumber == reader.serialNumber())
    }

    override suspend fun performTransaction(request: TransactionRequest, signatureProvider: SignatureProviding?, transactionUpdateListener: TransactionUpdateListener?): TransactionResult {
        TODO("Not yet implemented")
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        TODO("Not yet implemented")
    }

    override suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount?): TransactionResult {
        TODO("Not yet implemented")
    }

}