package com.fattmerchant.android.anywherecommerce

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.anywherecommerce.android.sdk.*
import com.anywherecommerce.android.sdk.devices.*
import com.anywherecommerce.android.sdk.util.Amount as ANPAmount
import com.anywherecommerce.android.sdk.devices.bbpos.BBPOSDevice
import com.anywherecommerce.android.sdk.endpoints.AnyPayTransaction
import com.anywherecommerce.android.sdk.endpoints.anywherecommerce.AnywhereCommerce
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetEndpoint
import com.anywherecommerce.android.sdk.models.Signature
import com.anywherecommerce.android.sdk.models.TransactionType
import com.anywherecommerce.android.sdk.services.CardReaderConnectionService
import com.anywherecommerce.android.sdk.transactions.listener.CardTransactionListener
import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.Transaction

import com.fattmerchant.omni.data.MobileReaderDriver.*
import com.fattmerchant.omni.data.models.Merchant
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.usecase.TakeMobileReaderPayment
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class AWCDriver: MobileReaderDriver {

    /** The endpoint that AnywhereCommerce will be reaching out to */
    private var endpoint: WorldnetEndpoint? = null

    /** The URL of the gateway that AnywhereCommerce will be reaching out to */
    private var gatewayUrl = "https://payments.anywherecommerce.com/merchant"

    /** The transaction currently underway */
    private var currentTransaction: AnyPayTransaction? = null

    override val source: String = "AWC"

    override var familiarSerialNumbers: MutableList<String> = mutableListOf()

    override var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    override suspend fun isReadyToTakePayment(): Boolean {
        // Make sure reader is connected
        val cardReader = CardReaderController.getConnectedReader() ?: return false

        // Make sure reader is idle
        cardReader.connectionStatus == ConnectionStatus.CONNECTED

        // Make sure there is no transaction running
        if (currentTransaction != null) { return false }

        return true
    }

    override suspend fun isOmniRefundsSupported(): Boolean {
        return false
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

    override suspend fun isInitialized(): Boolean = Terminal.isInitialized() && endpoint != null

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

    override suspend fun connectReader(reader: MobileReader): MobileReader? {
        // TODO("Need to implement this")
        return CardReaderController.getConnectedReader()?.toMobileReader()
    }

    override suspend fun disconnectReader(reader: MobileReader): Boolean {
        CardReaderController.getConnectedReader().disconnect()
        return true
    }

    override suspend fun performTransaction(request: TransactionRequest, signatureProvider: SignatureProviding?, transactionUpdateListener: TransactionUpdateListener?): TransactionResult {
        return suspendCancellableCoroutine { cancellableContinuation ->
            // Create AnyPayTransaction
            val transaction = AnyPayTransaction()
            transaction.transactionType = TransactionType.SALE
            transaction.currency = "USD"
            transaction.totalAmount = ANPAmount(request.amount.dollarsString())
            transaction.useCardReader(CardReaderController.getConnectedReader())

            // Register signature
            transaction.setOnSignatureRequiredListener {
                transactionUpdateListener?.onTransactionUpdate(TransactionUpdate.PromptProvideSignature)

                if (signatureProvider != null) {
                    signatureProvider.signatureRequired { signature ->
                        transactionUpdateListener?.onTransactionUpdate(TransactionUpdate.SignatureProvided)
                        transaction.signature = Signature() //TODO: Take care of the signature
                        transaction.proceed()
                    }
                } else {
                    transaction.proceed()
                }
            }

            currentTransaction = transaction
            transaction.execute(object : CardTransactionListener {
                override fun onCardReaderEvent(message: MeaningfulMessage?) {
                    message?.let {
                        TransactionUpdate.from(it)?.let { update ->
                            transactionUpdateListener?.onTransactionUpdate(update)
                        }
                    }
                }

                override fun onTransactionCompleted() {
                    val result = TransactionResult.from(transaction)
                    result.request = request
                    currentTransaction = null
                    cancellableContinuation.resume(result)
                }

                override fun onTransactionFailed(p0: MeaningfulError?) {
                    currentTransaction = null
                    cancellableContinuation.resumeWithException(PerformTransactionException(p0?.detail ?: "Transaction Failed"))
                }
            })
        }
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        TODO("Not yet implemented")
    }

    override suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount?): TransactionResult {
        TODO("Not yet implemented")
    }

    override suspend fun cancelCurrentTransaction(error: ((OmniException) -> Unit)?): Boolean {
        TODO("Not yet implemented")
    }

}
