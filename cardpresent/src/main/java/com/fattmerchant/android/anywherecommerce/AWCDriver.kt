package com.fattmerchant.android.anywherecommerce

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.os.Handler
import com.anywherecommerce.android.sdk.*
import com.anywherecommerce.android.sdk.devices.*
import com.anywherecommerce.android.sdk.endpoints.AnyPayTransaction
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetEndpoint
import com.anywherecommerce.android.sdk.models.Signature
import com.anywherecommerce.android.sdk.models.TransactionType
import com.anywherecommerce.android.sdk.transactions.listener.CardTransactionListener
import com.anywherecommerce.android.sdk.transactions.listener.TransactionListener
import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.MobileReaderDriver.*
import com.fattmerchant.omni.data.models.MobileReaderDetails
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.usecase.CancelCurrentTransactionException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.anywherecommerce.android.sdk.util.Amount as ANPAmount

internal class AWCDriver : MobileReaderDriver {

    /** The endpoint that AnywhereCommerce will be reaching out to */
    private var endpoint: WorldnetEndpoint? = null

    /** The URL of the gateway that AnywhereCommerce will be reaching out to */
    private var gatewayUrl = "https://payments.anywherecommerce.com/merchant"

    /** The transaction currently underway */
    private var currentTransaction: AnyPayTransaction? = null

    private var missingAwcDetails: Boolean = true

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

        val awcArgs = args["awc"] as? MobileReaderDetails.AWCDetails
            ?: throw InitializeMobileReaderDriverException("merchant not found")

        if (awcArgs.terminalId.isBlank() || awcArgs.terminalSecret.isBlank()) {
            missingAwcDetails = true
            throw InitializeMobileReaderDriverException("merchant not found")
        }

        // Initialize the Terminal. This will allow us to interact with AnyPay later on
        SDKManager.initialize(application)
        Terminal.initialize()

        // Create the endpoint
        val endpoint = Terminal.instance.endpoint as? WorldnetEndpoint
            ?: throw InitializeMobileReaderDriverException("Could not create worldnet endpoint")

        endpoint.worldnetTerminalID = awcArgs.terminalId
        endpoint.worldnetSecret = awcArgs.terminalSecret
        endpoint.gatewayUrl = gatewayUrl

        // Authenticate
        return suspendCancellableCoroutine {
            endpoint.authenticate(object : AuthenticationListener {
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

    override suspend fun isInitialized(): Boolean {
        if (missingAwcDetails) {
            return false
        }
        return Terminal.isInitialized() && endpoint != null
    }

    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        return suspendCancellableCoroutine {
            CardReader.connect(
                CardReader.ConnectionMethod.BLUETOOTH,
                object : CardReaderConnectionListener<CardReader>, MultipleBluetoothDevicesFoundListener {
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
                }
            )
        }
    }

    override suspend fun connectReader(reader: MobileReader): MobileReader? {
        // TODO("Need to implement this")
        return CardReaderController.getConnectedReader()?.toMobileReader()
    }

    override suspend fun getConnectedReader(): MobileReader? =
        CardReaderController.getConnectedReader()?.toMobileReader()

    override suspend fun disconnect(reader: MobileReader, error: (OmniException) -> Unit): Boolean {

        if (CardReaderController.isCardReaderConnected() && CardReaderController.getConnectedReader().connectionMethod == CardReader.ConnectionMethod.BLUETOOTH) {
            CardReaderController.getConnectedReader()?.disconnect() ?: run {
                error(OmniException("Unable to disconnect reader", "Card reader is null"))
            }

            return true
        } else {
            // In case of no connected reader
            return false
        }
    }

    var transactionUpdateDelay = 0.0

    override suspend fun performTransaction(request: TransactionRequest, signatureProvider: SignatureProviding?, transactionUpdateListener: TransactionUpdateListener?, userNotificationListener: UserNotificationListener?): TransactionResult {
        return suspendCancellableCoroutine { cancellableContinuation ->
            // Create AnyPayTransaction
            val transaction = AnyPayTransaction()
            transaction.transactionType = TransactionType.SALE
            transaction.currency = "USD"
            transaction.totalAmount = ANPAmount(request.amount.dollarsString())
            transaction.useCardReader(CardReaderController.getConnectedReader())

            // Register signature
            if (signatureProvider != null) {
                transaction.setOnSignatureRequiredListener {
                    transactionUpdateListener?.onTransactionUpdate(TransactionUpdate.PromptProvideSignature)
                    signatureProvider.signatureRequired {
                        transactionUpdateListener?.onTransactionUpdate(TransactionUpdate.SignatureProvided)
                        transaction.signature = Signature() // TODO: Take care of the signature
                        transaction.proceed()
                    }
                }
            } else {
                transaction.proceed()
            }

            currentTransaction = transaction
            transaction.execute(object : CardTransactionListener {
                override fun onCardReaderEvent(message: MeaningfulMessage?) {
                    message?.let {
                        // Ignore the PRESENT_CARD message
                        if (message.toString() == "PRESENT_CARD") { return@let }

                        val transactionUpdate = TransactionUpdate.from(it)
                        if (transactionUpdate != null) {
                            Handler().postDelayed({
                                transactionUpdateListener?.onTransactionUpdate(transactionUpdate)
                                transactionUpdateDelay = 0.0
                            }, transactionUpdateDelay.toLong())
                        } else {
                            transactionUpdateDelay = 2.0 * 1000
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

    override suspend fun capture(transaction: Transaction): Boolean {
        return true // transactions with AWC are autocaptured
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        return TransactionResult()
    }

    override fun voidTransaction(
        transactionResult: TransactionResult,
        completion: (Boolean) -> Unit
    ) {
        completion(false)
    }

    override suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount?): TransactionResult {
        return suspendCancellableCoroutine { continuation ->
            val refund = AnyPayTransaction()
            refund.endpoint = endpoint
            refund.transactionType = TransactionType.REFUND
            refund.externalId = transaction.awcExternalId()
            refund.refTransactionId = transaction.awcExternalId()
            refund.totalAmount = ANPAmount(refundAmount?.dollarsString())

            refund.execute(object : TransactionListener {
                override fun onTransactionCompleted() {
                    val result = TransactionResult.from(refund)

                    // AWC is returning a negative approved amount so we want to use our total instead
                    if (refund.approvedAmount.isLessThan(0.00)) {
                        result.amount = refundAmount
                    }

                    continuation.resume(result)
                }

                override fun onTransactionFailed(p0: MeaningfulError?) {
                    continuation.resumeWithException(RefundTransactionException(p0?.detail ?: "Could not perform refund"))
                }
            })
        }
    }

    override suspend fun cancelCurrentTransaction(error: ((OmniException) -> Unit)?): Boolean {
        currentTransaction?.let {
            it.cancel()
            return true
        }
        error?.invoke(CancelCurrentTransactionException("Could not cancel current transaction"))
        return false
    }
}
