package com.staxpayments.android.chipdna

import android.content.Context
import com.creditcall.chipdnamobile.*
import com.staxpayments.exceptions.CancelCurrentTransactionException
import com.staxpayments.exceptions.ConnectReaderException
import com.staxpayments.exceptions.InitializeMobileReaderDriverException
import com.staxpayments.exceptions.PerformTransactionException
import com.staxpayments.exceptions.RefundTransactionException
import com.staxpayments.exceptions.StaxException
import com.staxpayments.exceptions.StaxGeneralException
import com.staxpayments.sdk.MobileReaderConnectionStatusListener
import com.staxpayments.sdk.SignatureProviding
import com.staxpayments.sdk.TransactionUpdateListener
import com.staxpayments.sdk.UserNotificationListener
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.MobileReaderDriver
import com.staxpayments.sdk.data.TransactionRequest
import com.staxpayments.sdk.data.TransactionResult
import com.staxpayments.api.models.MobileReaderConnectionStatus
import com.staxpayments.api.models.MobileReaderDetails
import com.staxpayments.api.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Locale
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChipDnaDriver :
    CoroutineScope,
    MobileReaderDriver,
    IConfigurationUpdateListener,
    IDeviceUpdateListener {

    class NmiConnectReaderException(message: String? = null) :
        ConnectReaderException(mapDetailMessage(message)) {
        companion object {
            fun mapDetailMessage(chipDnaMessage: String?): String? {
                return when (chipDnaMessage) {
                    "ConnectionClosed" -> "Connection closed"
                    "BluetoothNotEnabled" -> "Bluetooth not enabled"
                    else -> chipDnaMessage
                }
            }
        }
    }

    companion object {
        /**
         * Attempts to get the connected mobile reader.
         * @return the connected [MobileReader], if found. Null otherwise
         */
        internal fun getConnectedReader(): MobileReader? {
            val chipDnaMobileStatus = ChipDnaMobile.getInstance().getStatus(null)
            val deviceStatusXml = chipDnaMobileStatus[ParameterKeys.DeviceStatus] ?: return null
            val deviceStatus = ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)

            return when (deviceStatus.status) {
                DeviceStatus.DeviceStatusEnum.DeviceStatusConnected -> mapDeviceStatusToMobileReader(deviceStatus)
                else -> null
            }
        }
    }

    class SelectablePinPad(var name: String, var connectionType: String)

    /** A key used to communicate with TransactionGateway */
    private var securityKey: String = ""

    override var familiarSerialNumbers: MutableList<String> = mutableListOf()
    override val source: String = "NMI"
    override var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    val log = Logger.getLogger("ChipDNA")
    fun log(msg: String?) {
        log.info("[${Thread.currentThread().name}] $msg")
    }

    /**
     * This is the data that we will need in order to initialize ChipDna again if something
     * happens at runtime.
     *
     * For example, if the user wants to disconnect a reader, we have to use
     * the ChipDnaMobile.dispose() method. This method uninitializes the SDK and we have to
     * initialize again if we want to reconnect a reader. When we want to reconnect, we use these
     * args
     * */
    private var initArgs: Map<String, Any> = mapOf()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    override suspend fun initialize(args: Map<String, Any>): Boolean {
        // Make sure we have all the necessary data
        val appContext = args["appContext"] as? Context
            ?: throw InitializeMobileReaderDriverException("appContext not found")

        val appId = args["appId"] as? String
            ?: throw InitializeMobileReaderDriverException("appId not found")

        val nmiDetails = args["nmi"] as? MobileReaderDetails.NMIDetails
            ?: throw InitializeMobileReaderDriverException("nmi details not found")

        val apiKey = nmiDetails.securityKey

        if (apiKey.isBlank()) {
            throw InitializeMobileReaderDriverException("emvTerminalSecret not found")
        }

        val params = Parameters().apply {
            add(ParameterKeys.Password, "password")
        }

        // Init
        ChipDnaMobile.initialize(appContext, params)

        // Start listening to configuration updates
        ChipDnaMobile.getInstance().addConfigurationUpdateListener(this)
        ChipDnaMobile.getInstance().addDeviceUpdateListener(this)

        // Store security key and init args for later use
        securityKey = apiKey
        initArgs = args

        // Set credentials
        val result = setCredentials(appId, apiKey)
        if (result[ParameterKeys.Result] != ParameterValues.TRUE) {
            throw InitializeMobileReaderDriverException("Invalid credentials for reader")
        }

        val output = result[ParameterKeys.Result] == ParameterValues.TRUE
        return output
    }

    override suspend fun isStaxRefundsSupported() = true

    /**
     * Checks if the ChipDna instance has been successfully initialized
     * @return true if the ChipDna instance has been successfully initialized
     */
    override suspend fun isInitialized(): Boolean {
        return ChipDnaMobile.isInitialized()
    }

    /**
     * Searches for available readers over BT, BLE, and USB. Then, parses out the available readers
     * into a Stax usable form.
     * @return A list of [MobileReader] devices
     */
    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        val selectablePinPads = suspendCancellableCoroutine { cont ->
            ChipDnaMobile.getInstance().apply {
                clearAllAvailablePinPadsListeners()
                addAvailablePinPadsListener { params ->
                    val availablePinPadsXml = params.getValue(ParameterKeys.AvailablePinPads)
                    val pinPads = deserializePinPads(availablePinPadsXml)
                    cont.resume(pinPads)
                }
            }.getAvailablePinPads(
                Parameters().apply {
                    add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.TRUE)
                    add(ParameterKeys.SearchConnectionTypeBluetoothLe, ParameterValues.TRUE)
                    add(ParameterKeys.SearchConnectionTypeUsb, ParameterValues.TRUE)
                }
            )
        }

        return selectablePinPads.map { pinPad ->
            val connectionType = if (pinPad.connectionType.equals(
                    ParameterValues.BluetoothConnectionType,
                    ignoreCase = true
                )
            ) {
                ConnectionType.BT
            } else if (pinPad.connectionType.equals(
                    ParameterValues.BluetoothLeConnectionType,
                    ignoreCase = true
                )
            ) {
                ConnectionType.BLE
            } else if (pinPad.connectionType.equals(
                    ParameterValues.UsbConnectionType,
                    ignoreCase = true
                )
            ) {
                ConnectionType.USB
            } else {
                ConnectionType.UNKNOWN
            }

            mapPinPadToMobileReader(pinPad, connectionType)
        }
    }

    /**
     * Attempts to connect to a specified mobile reader
     * @return the mobile reader object (if it is successfully connected to), or null
     */
    override suspend fun connectReader(reader: MobileReader): MobileReader? {
        val connectionType: String = if (reader.getConnectionType() == ConnectionType.BT) {
            ParameterValues.BluetoothConnectionType
        } else if (reader.getConnectionType() == ConnectionType.BLE) {
            ParameterValues.BluetoothLeConnectionType
        } else if (reader.getConnectionType() == ConnectionType.USB) {
            ParameterValues.UsbConnectionType
        } else { // Default to BLE
            ParameterValues.BluetoothLeConnectionType
        }

        // Set properties of reader to connect to here. Adding them as
        // connectAndConfigure params causes it to never connect correctly
        ChipDnaMobile.getInstance().setProperties(
            Parameters().apply {
                add(ParameterKeys.PinPadName, reader.getName())
                add(ParameterKeys.PinPadConnectionType, connectionType)
            }
        )

        return suspendCancellableCoroutine { continuation ->
            val connectAndConfigureParams = ChipDnaMobile.getInstance().getStatus(null)
            ChipDnaMobile.getInstance().apply {
                clearAllConnectAndConfigureFinishedListeners()
                addConnectAndConfigureFinishedListener { params ->
                    if (params[ParameterKeys.Result] == ParameterValues.TRUE) {
                        Companion.getConnectedReader()?.let { connectedReader ->
                            connectedReader.serialNumber()?.let { familiarSerialNumbers.add(it) }
                            continuation.resume(connectedReader)
                        }
                        return@addConnectAndConfigureFinishedListener
                    }

                    val error = params[ParameterKeys.ErrorDescription]
                    continuation.resumeWithException(NmiConnectReaderException(error))
                }
            }.connectAndConfigure(connectAndConfigureParams)
        }
    }

    override suspend fun disconnect(reader: MobileReader, error: (StaxException) -> Unit): Boolean {
        ChipDnaMobile.dispose(null)
        mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(MobileReaderConnectionStatus.DISCONNECTED)
        initialize(initArgs)
        return true
    }

    override suspend fun getConnectedReader(): MobileReader? {
        // ChipDna must be initialized
        if (!ChipDnaMobile.isInitialized()) {
            throw StaxGeneralException.uninitialized
        }

        return Companion.getConnectedReader()
    }

    override suspend fun isReadyToTakePayment(): Boolean {
        // ChipDna must be initialized
        if (!ChipDnaMobile.isInitialized()) {
            return false
        }

        try {
            val status = ChipDnaMobile.getInstance().getStatus(null)

            // Device must be connected
            val deviceStatusXml = status[ParameterKeys.DeviceStatus] ?: return false
            val deviceStatus = ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)
            if (deviceStatus.status != DeviceStatus.DeviceStatusEnum.DeviceStatusConnected) {
                return false
            }

            // Terminal must be enabled
            val terminalStatusXml = status[ParameterKeys.TerminalStatus] ?: return false
            val terminalStatus = ChipDnaMobileSerializer.deserializeTerminalStatus(terminalStatusXml)
            if (!terminalStatus.isEnabled) {
                return false
            }
        } catch (e: Throwable) {
            return false
        }

        return true
    }

    override suspend fun performTransaction(request: TransactionRequest, signatureProvider: SignatureProviding?, transactionUpdateListener: TransactionUpdateListener?, userNotificationListener: UserNotificationListener?): TransactionResult {
        val paymentRequestParams = withTransactionRequest(request)

        val result = suspendCancellableCoroutine<Parameters> { cont ->

            val transactionListener = ChipDnaTransactionListener()
            transactionListener.onFinish = {
                ChipDnaMobile.getInstance().removeTransactionUpdateListener(transactionListener)
                ChipDnaMobile.getInstance().removeTransactionFinishedListener(transactionListener)
                ChipDnaMobile.getInstance().removeDeferredAuthorizationListener(transactionListener)
                ChipDnaMobile.getInstance().removeSignatureVerificationListener(transactionListener)
                ChipDnaMobile.getInstance().removeVoiceReferralListener(transactionListener)
                ChipDnaMobile.getInstance().removePartialApprovalListener(transactionListener)
                ChipDnaMobile.getInstance().removeForceAcceptanceListener(transactionListener)
                ChipDnaMobile.getInstance().removeVerifyIdListener(transactionListener)
                cont.resume(it)
            }

            transactionListener.signatureProvider = signatureProvider
            transactionListener.transactionUpdateListener = transactionUpdateListener
            transactionListener.userNotificationListener = userNotificationListener
            ChipDnaMobile.getInstance().addUserNotificationListener(transactionListener)
            ChipDnaMobile.getInstance().addApplicationSelectionListener(transactionListener)
            ChipDnaMobile.getInstance().addTransactionUpdateListener(transactionListener)
            ChipDnaMobile.getInstance().addTransactionFinishedListener(transactionListener)
            ChipDnaMobile.getInstance().addDeferredAuthorizationListener(transactionListener)
            ChipDnaMobile.getInstance().addSignatureVerificationListener(transactionListener)
            ChipDnaMobile.getInstance().addVoiceReferralListener(transactionListener)
            ChipDnaMobile.getInstance().addPartialApprovalListener(transactionListener)
            ChipDnaMobile.getInstance().addForceAcceptanceListener(transactionListener)
            ChipDnaMobile.getInstance().addVerifyIdListener(transactionListener)

            val response = ChipDnaMobile.getInstance().startTransaction(paymentRequestParams)
            // TODO: Handle the case where ChipDnaMobile didn't actually start the transaction
        }

        // Check for errors
        if (result.containsKey(ParameterKeys.Errors)) {
            // Transaction was _not_ declined. Handle error accordingly
            when {
                result[ParameterKeys.Errors]?.contains("PinPadUserCancelled") == true -> {
                    throw PerformTransactionException("User cancelled transaction")
                }

                result[ParameterKeys.Errors]?.contains("InvalidCardResponse") == true -> {
                    throw PerformTransactionException("Invalid card response")
                }

                else -> {
                    throw PerformTransactionException(
                        result[ParameterKeys.ErrorDescription] ?: "Unknown error performing mobile reader transaction"
                    )
                }
            }
        }

        // Check to see if transaction actually failed, or if it was declined
        if (result[ParameterKeys.TransactionResult] == ParameterValues.Declined) {
            when {
                result[ParameterKeys.Errors]?.contains("GatewayRejectedTransaction") == true -> {
                    throw PerformTransactionException("Gateway rejected transaction")
                }
            }
        }

        // Build the TransactionResult and return
        val firstName = result[ParameterKeys.CardHolderFirstName]
        val lastName = result[ParameterKeys.CardHolderLastName]
        val addressZip = result[ParameterKeys.BillingZipCode]
        val address1 = result[ParameterKeys.BillingAddress1]
        val address2 = result[ParameterKeys.BillingAddress2]
        val addressState = result[ParameterKeys.BillingState]
        var ccExpiration: String? = null

        val receiptData = ChipDnaMobileSerializer.deserializeReceiptData(result[ParameterKeys.ReceiptData])

        // Try to add the cc expiration
        result[ParameterKeys.TransactionId]?.let { transactionId ->
            ccExpiration = TransactionGateway.getTransactionCcExpiration(securityKey, transactionId)
        }

        return TransactionResult().apply {
            this.request = request
            authCode = result[ParameterKeys.AuthCode]
            maskedPan = result[ParameterKeys.MaskedPan] ?: ""
            userReference = result[ParameterKeys.UserReference]
            localId = result[ParameterKeys.CardEaseReference]
            externalId = result[ParameterKeys.TransactionId]
            cardHolderFirstName = firstName
            cardHolderLastName = lastName
            cardType = result[ParameterKeys.CardSchemeId]?.lowercase(Locale.ROOT)
            cardExpiration = ccExpiration
            amount = Amount(cents = result[ParameterKeys.Amount]?.toInt() ?: request.amount.cents)

            this.source = this@ChipDnaDriver.source
            success = result[ParameterKeys.TransactionResult] == ParameterValues.Approved
            transactionSource = receiptData[ReceiptFieldKey.TRANSACTION_SOURCE]?.value

            result[ParameterKeys.CustomerVaultId]?.let { token ->
                paymentToken = "nmi_$token"
            }
        }
    }

    override suspend fun capture(transaction: Transaction): Boolean {
        val userRef = extractUserReference(transaction) ?: return false

        val params = Parameters().apply {
            add(ParameterKeys.UserReference, userRef)
        }

        val result = ChipDnaMobile.getInstance().confirmTransaction(params)
        return result[ParameterKeys.TransactionResult] == ParameterValues.Approved
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        val ref = extractCardEaseReference(transaction)

        val voidRequestParams = Parameters().apply {
            add(ParameterKeys.UserReference, ref)
        }

        var response = ChipDnaMobile.getInstance().voidTransaction(voidRequestParams)

        // TODO: Handle errors

        return TransactionResult()
    }

    override fun voidTransaction(transactionResult: TransactionResult, completion: (Boolean) -> Unit) {
        val userRef = transactionResult.userReference ?: return completion(false)

        val params = Parameters().apply {
            add(ParameterKeys.UserReference, userRef)
        }

        val result = ChipDnaMobile.getInstance().voidTransaction(params)
        completion(result[ParameterKeys.Result] == ParameterValues.Approved)
    }

    override suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount?): TransactionResult {
        // Prepare Parameters for refunding
        val ref = extractCardEaseReference(transaction)

        val amountCents = refundAmount?.cents ?: transaction.total?.toFloat()?.times(100)?.toInt() ?: 0
        val refundRequestParams = Parameters().apply {
            add(ParameterKeys.UserReference, generateUserReference())
            add(ParameterKeys.CardEaseReference, ref)
            add(ParameterKeys.Amount, amountCents)
            add(ParameterKeys.Currency, "USD")
        }

        val result = ChipDnaMobile.getInstance().linkedRefundTransaction(refundRequestParams)

        return if (result[ParameterKeys.TransactionResult] == ParameterValues.Approved) {
            TransactionResult().apply {
                this.request = request
                success = true
                transactionType = "refund"
                amount = Amount(cents = amountCents)
            }
        } else {
            throw RefundTransactionException(result[ParameterKeys.ErrorDescription] ?: "Could not refund transaction")
        }
    }

    override suspend fun cancelCurrentTransaction(error: ((StaxException) -> Unit)?): Boolean {
        val result = ChipDnaMobile.getInstance().terminateTransaction(null)
        result?.let {
            val success = result[ParameterKeys.Result] == ParameterValues.TRUE
            if (success) {
                return success
            } else {
                val status = ChipDnaMobile.getInstance().getStatus(null)
                val idle = status[ParameterKeys.ChipDnaStatus] == "IDLE"
                if (idle) {
                    error?.invoke(CancelCurrentTransactionException.NoTransactionToCancel)
                } else {
                    error?.invoke(CancelCurrentTransactionException.Unknown)
                }
            }
        }
        return false
    }

    private fun setCredentials(appId: String, apiKey: String): Parameters {
        val params = Parameters().apply {
            add(ParameterKeys.ApiKey, apiKey)
            add(ParameterKeys.Environment, ParameterValues.LiveEnvironment)
            add(ParameterKeys.ApplicationIdentifier, appId)
        }
        return ChipDnaMobile.getInstance().setProperties(params)
    }

    private fun deserializePinPads(pinPadsXml: String?): List<SelectablePinPad> {
        if (pinPadsXml.isNullOrEmpty()) {
            return listOf()
        }

        val pinPadsList = mutableListOf<SelectablePinPad>()
        try {
            val pinPadsMap = ChipDnaMobileSerializer.deserializeAvailablePinPads(pinPadsXml)
            for (connectionType in pinPadsMap.keys) {
                pinPadsMap[connectionType]?.let { pinPads ->
                    for (pinPad in pinPads) {
                        val prettyConnectionType = if (connectionType.equals(
                                ParameterValues.BluetoothConnectionType,
                                ignoreCase = true
                            )
                        ) {
                            "[BT]"
                        } else if (connectionType.equals(
                                ParameterValues.BluetoothLeConnectionType,
                                ignoreCase = true
                            )
                        ) {
                            "[BLE]"
                        } else if (connectionType.equals(
                                ParameterValues.UsbConnectionType,
                                ignoreCase = true
                            )
                        ) {
                            "[USB]"
                        } else {
                            connectionType
                        }
                        pinPadsList.add(SelectablePinPad(pinPad, connectionType))
                    }
                }
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return pinPadsList
    }

    override fun onConfigurationUpdateListener(parameters: Parameters?) {
        parameters[ParameterKeys.ConfigurationUpdate]?.let {
            MobileReaderConnectionStatus.from(it)?.let {
                mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(it)
            }
        }
    }

    override fun onDeviceUpdate(parameters: Parameters?) {
        parameters[ParameterKeys.DeviceStatusUpdate]?.let { deviceStatusXml ->
            ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)?.let { deviceStatus ->
                MobileReaderConnectionStatus.from(deviceStatus.status)?.let {
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(it)
                }
            }
        }
    }
}
