package com.fattmerchant.android.chipdna

import android.content.Context
import com.creditcall.chipdnamobile.ChipDnaMobile
import com.creditcall.chipdnamobile.ChipDnaMobileSerializer
import com.creditcall.chipdnamobile.DeviceStatus
import com.creditcall.chipdnamobile.IConfigurationUpdateListener
import com.creditcall.chipdnamobile.IDeviceUpdateListener
import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.ParameterValues
import com.creditcall.chipdnamobile.Parameters
import com.creditcall.chipdnamobile.ReceiptFieldKey
import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.OmniGeneralException
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.MobileReaderDriver.InitializeMobileReaderDriverException
import com.fattmerchant.omni.data.MobileReaderDriver.PerformTransactionException
import com.fattmerchant.omni.data.MobileReaderDriver.RefundTransactionException
import com.fattmerchant.omni.data.TapToPayConfiguration
import com.fattmerchant.omni.data.TapToPayReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionResult
import com.fattmerchant.omni.data.models.MobileReaderConnectionStatus
import com.fattmerchant.omni.data.models.MobileReaderDetails
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.usecase.CancelCurrentTransactionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Locale
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class ChipDnaDriver :
    CoroutineScope,
    MobileReaderDriver,
    IConfigurationUpdateListener,
    IDeviceUpdateListener {

    class ConnectReaderException(message: String? = null) : MobileReaderDriver.ConnectReaderException(mapDetailMessage(message)) {
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
         *
         * @param chipDnaMobileStatus the result of [ChipDnaMobile.getStatus]. If none is provided, this
         * method will retrieve it
         * @param tapToPayConfig the Tap to Pay configuration to check for NFC mode
         * @return the connected [MobileReader], if found. Null otherwise
         */
        internal fun getConnectedReader(
            chipDnaMobileStatus: Parameters? = ChipDnaMobile.getInstance().getStatus(null),
            tapToPayConfig: TapToPayConfiguration? = null,
            isTapToPayReaderConnected: Boolean = false
        ): MobileReader? {
            // If Tap to Pay reader is explicitly connected, return it
            if (isTapToPayReaderConnected && tapToPayConfig?.enabled == true) {
                if (ChipDnaMobile.isInitialized()) {
                    return TapToPayReader()
                }
            }
            
            // Check for physical connected reader
            val deviceStatusXml = chipDnaMobileStatus?.get(ParameterKeys.DeviceStatus) ?: return null
            val deviceStatus = ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)

            return when (deviceStatus.status) {
                DeviceStatus.DeviceStatusEnum.DeviceStatusConnected -> mapDeviceStatusToMobileReader(deviceStatus)
                else -> null
            }
        }
    }

    inner class SelectablePinPad(var name: String, var connectionType: String)

    /** A key used to communicate with TransactionGateway */
    private var securityKey: String = ""
    
    /** Tap to Pay configuration */
    private var tapToPayConfig: TapToPayConfiguration? = null
    
    /** Activity delegate for NFC operations in Tap to Pay mode */
    private var requestActivityDelegate: RequestActivityDelegate? = null
    
    /** Tracks whether the TapToPayReader is currently connected */
    private var isTapToPayReaderConnected: Boolean = false
    
    /** Callback for when terminal configuration completes */
    private var onTerminalReadyCallback: (() -> Unit)? = null

    override var familiarSerialNumbers: MutableList<String> = mutableListOf()
    override val source: String = "NMI"
    override var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    private val logger = Logger.getLogger("ChipDNA")
    fun log(msg: String?) {
        logger.info("[${Thread.currentThread().name}] $msg")
    }

    /**
     * This is the data that we will need in order to initialize ChipDna again if something
     * happens at runtime.
     *
     * For example, if the user wants to disconnect a reader, we have to use
     * the ChipDnaMobile.dispose() method. This method un-initializes the SDK and we have to
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
        
        // Get Tap to Pay configuration if provided
        tapToPayConfig = args["tapToPayConfig"] as? TapToPayConfiguration

        val params = Parameters().apply {
            add(ParameterKeys.Password, "password")
            add(ParameterKeys.AutoConfirm, ParameterValues.TRUE)
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
        
        // Log detailed result for debugging
        log("setProperties result: ${result[ParameterKeys.Result]}")
        result[ParameterKeys.ErrorDescription]?.let { log("Error: $it") }
        result[ParameterKeys.Errors]?.let { log("Errors: $it") }
        result["ErrorCode"]?.let { log("ErrorCode: $it") }
        
        // Check terminal status immediately after setProperties
        val statusAfterSet = ChipDnaMobile.getInstance().getStatus(null)
        val terminalStatusXml = statusAfterSet[ParameterKeys.TerminalStatus]
        terminalStatusXml?.let {
            val terminalStatus = ChipDnaMobileSerializer.deserializeTerminalStatus(it)
            log("Terminal status after setProperties: isEnabled=${terminalStatus.isEnabled}")
        }
        
        if (result[ParameterKeys.Result] != ParameterValues.TRUE) {
            val errorMsg = result[ParameterKeys.ErrorDescription] ?: "Invalid credentials for reader"
            throw InitializeMobileReaderDriverException(errorMsg)
        }

        return result[ParameterKeys.Result] == ParameterValues.TRUE
    }

    override suspend fun isOmniRefundsSupported(): Boolean {
        return true
    }

    override suspend fun isInitialized(): Boolean {
        val isInitialized = ChipDnaMobile.isInitialized()
        if (!isInitialized && initArgs.isNotEmpty()) {
            async { initialize(initArgs) }.await()
            return ChipDnaMobile.isInitialized()
        }
        return isInitialized
    }

    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        if (!isInitialized()) {
            if (initArgs.isNotEmpty()) {
                async { initialize(initArgs) }.await()
            } else {
                throw OmniGeneralException.uninitialized
            }
        }
        
        val readersList = mutableListOf<MobileReader>()
        
        // If Tap to Pay is enabled, add the virtual Tap to Pay reader
        if (isTapToPayEnabled()) {
            log("Adding Tap to Pay reader to available readers")
            readersList.add(TapToPayReader())
        }
        
        // If external readers are allowed or Tap to Pay is disabled, search for physical readers
        if (tapToPayConfig?.allowExternalReaders != false || !isTapToPayEnabled()) {
            val externalReaders = searchForExternalReaders()
            readersList.addAll(externalReaders)
        }
        
        return readersList
    }
    
    /**
     * Searches for external Bluetooth/USB readers.
     * This is the original searchForReaders logic for physical devices.
     */
    private suspend fun searchForExternalReaders(): List<MobileReader> {
        val readers = suspendCancellableCoroutine { continuation ->
            ChipDnaMobile.getInstance().apply {
                clearAllAvailablePinPadsListeners()
                addAvailablePinPadsListener { params ->
                    val xml = params.getValue(ParameterKeys.AvailablePinPads)
                    val pads = deserializePinPads(xml)
                    continuation.resume(pads)
                }
            }.getAvailablePinPads(
                Parameters().apply {
                    add(ParameterKeys.SearchConnectionTypeBluetoothLe, ParameterValues.TRUE)
                    add(ParameterKeys.SearchConnectionTypeUsb, ParameterValues.TRUE)
                    // TODO: Further testing without legacy bluetooth
                    // add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.TRUE)
                }
            )
        }

        return readers.map { reader ->
            val type = ConnectionType.parse(reader.connectionType)
            mapPinPadToMobileReader(reader, type)
        }
    }

    override suspend fun connectReader(reader: MobileReader): MobileReader? {
        if (!isInitialized()) {
            if (initArgs.isNotEmpty()) {
                async { initialize(initArgs) }.await()
            } else {
                throw OmniGeneralException.uninitialized
            }
        }

        // Tap to Pay needs connectAndConfigure to activate NFC reader
        if (reader is TapToPayReader) {
            log("Tap to Pay reader selected - calling connectAndConfigure")
            
            return suspendCancellableCoroutine { continuation ->
                // If mock mode is enabled, bypass terminal status check and simulate immediate success
                if (tapToPayConfig?.mockMode == true) {
                    log("MOCK MODE: Bypassing terminal.isEnabled check and simulating connection")
                    log("MOCK MODE: This allows testing the NFC prompt with Test Card Simulator")
                    log("MOCK MODE: Real transactions will still fail without NMI onboarding")
                    doConnectAndConfigure(reader, continuation)
                    return@suspendCancellableCoroutine
                }
                
                // Check if terminal is already enabled
                val status = ChipDnaMobile.getInstance().getStatus(null)
                val terminalStatusXml = status[ParameterKeys.TerminalStatus]
                val terminalStatus = terminalStatusXml?.let { 
                    ChipDnaMobileSerializer.deserializeTerminalStatus(it) 
                }
                
                log("Terminal status before connect: isEnabled=${terminalStatus?.isEnabled}")
                
                // If terminal is not enabled, wait for configuration to complete
                if (terminalStatus?.isEnabled != true) {
                    log("Terminal not enabled yet - waiting for configuration to complete...")
                    
                    // Set callback for when configuration completes
                    onTerminalReadyCallback = {
                        log("Terminal is ready via callback - now calling connectAndConfigure")
                        doConnectAndConfigure(reader, continuation)
                    }
                    
                    // Also poll terminal status every 2 seconds in case we miss the config event
                    val startTime = System.currentTimeMillis()
                    val pollJob = async {
                        while (System.currentTimeMillis() - startTime < 30000) { // 30 second timeout
                            delay(2000) // Poll every 2 seconds
                            val currentStatus = ChipDnaMobile.getInstance().getStatus(null)
                            val currentTerminalXml = currentStatus[ParameterKeys.TerminalStatus]
                            val currentTerminal = currentTerminalXml?.let {
                                ChipDnaMobileSerializer.deserializeTerminalStatus(it)
                            }
                            
                            if (currentTerminal?.isEnabled == true) {
                                log("Terminal is ready via polling - now calling connectAndConfigure")
                                onTerminalReadyCallback = null // Clear callback
                                doConnectAndConfigure(reader, continuation)
                                return@async
                            } else {
                                log("Polling: Terminal still not enabled (isEnabled=${currentTerminal?.isEnabled})")
                            }
                        }
                        // Timeout reached
                        log("Timeout waiting for terminal to become enabled")
                        onTerminalReadyCallback = null
                        continuation.resumeWith(Result.failure(
                            ConnectReaderException("Timeout waiting for terminal configuration to complete")
                        ))
                    }
                    
                    continuation.invokeOnCancellation {
                        pollJob.cancel()
                        onTerminalReadyCallback = null
                    }
                    
                    return@suspendCancellableCoroutine
                }
                
                // Terminal is already enabled, proceed immediately
                log("Terminal already enabled - calling connectAndConfigure immediately")
                doConnectAndConfigure(reader, continuation)
            }
        }
        
        // For physical readers below...


        // Set properties of reader to connect to here. Adding them as
        // connectAndConfigure params causes it to never connect correctly
        ChipDnaMobile.getInstance().setProperties(
            Parameters().apply {
                add(ParameterKeys.PinPadName, reader.getName())
                add(ParameterKeys.PinPadConnectionType, reader.getConnectionType().toParameterValue())
            }
        )

        return suspendCancellableCoroutine { continuation ->
            val connectAndConfigureParams = ChipDnaMobile.getInstance().getStatus(null)
            ChipDnaMobile.getInstance().apply {
                clearAllConnectAndConfigureFinishedListeners()
                addConnectAndConfigureFinishedListener { params ->
                    if (params[ParameterKeys.Result] == ParameterValues.TRUE) {
                        getConnectedChipDnaReader()?.let { connectedReader ->
                            connectedReader.serialNumber()?.let { familiarSerialNumbers.add(it) }
                            continuation.resume(connectedReader)
                        }
                        return@addConnectAndConfigureFinishedListener
                    }

                    val error = params[ParameterKeys.ErrorDescription]
                    continuation.resumeWithException(ConnectReaderException(error))
                }
            }.connectAndConfigure(connectAndConfigureParams)
        }
    }

    private fun doConnectAndConfigure(
        reader: TapToPayReader,
        continuation: kotlin.coroutines.Continuation<MobileReader?>
    ) {
        // Get current status and add Tap to Pay parameters based on configuration
        val currentStatus = ChipDnaMobile.getInstance().getStatus(null)
        
        val tapToMobilePOI = tapToPayConfig?.getTapToMobilePOIValue() ?: ParameterValues.TRUE
        val paymentDevicePOI = tapToPayConfig?.getPaymentDevicePOIValue() ?: ParameterValues.FALSE
        
        currentStatus.apply {
            add(ParameterKeys.TapToMobilePOI, tapToMobilePOI)
            add(ParameterKeys.PaymentDevicePOI, paymentDevicePOI)
            log("Added TapToMobilePOI: $tapToMobilePOI, PaymentDevicePOI: $paymentDevicePOI")
            
            if (paymentDevicePOI == ParameterValues.TRUE) {
                log("Hybrid mode: Both Tap to Pay and external readers enabled via connectAndConfigure")
            } else {
                log("Tap to Pay only mode: External readers disabled via connectAndConfigure")
            }
        }
        
        ChipDnaMobile.getInstance().apply {
            clearAllConnectAndConfigureFinishedListeners()
            
            addConnectAndConfigureFinishedListener { params ->
                log("connectAndConfigure finished callback received")
                log("Result: ${params[ParameterKeys.Result]}")
                params[ParameterKeys.ErrorDescription]?.let { log("ErrorDescription: $it") }
                
                if (params[ParameterKeys.Result] == ParameterValues.TRUE) {
                    log("Tap to Pay configuration completed successfully")
                    isTapToPayReaderConnected = true
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(
                        MobileReaderConnectionStatus.CONNECTED
                    )
                    continuation.resumeWith(Result.success(reader))
                } else {
                    val error = params[ParameterKeys.ErrorDescription] ?: "Unknown error"
                    log("Tap to Pay configuration failed: $error")
                    continuation.resumeWith(Result.failure(ConnectReaderException(error)))
                }
            }
            
            log("Calling connectAndConfigure...")
        }.connectAndConfigure(currentStatus)
    }
    
    override suspend fun disconnect(reader: MobileReader?, error: (OmniException) -> Unit): Boolean {
        val retryLimit = 3
        val retryTimes = arrayOf(500L, 1000L, 2000L)
        var retryCount = 0

        do {
            // Check the result. If the disconnect was successful, break out of the retry loop.
            val result = ChipDnaMobile.dispose(null)
            if (result[ParameterKeys.Result] == ParameterValues.TRUE) {
                isTapToPayReaderConnected = false
                mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(MobileReaderConnectionStatus.DISCONNECTED)
                return true
            }

            // Wait a beat and retry. The failure is due to another transaction in progress like canceling a transaction.
            async { delay(retryTimes[retryCount]) }.await()
            retryCount++
        } while (retryCount < retryLimit)

        return false
    }

    override suspend fun getConnectedReader(): MobileReader? {
        log("getConnectedReader called: isTapToPayReaderConnected=$isTapToPayReaderConnected")
        // ChipDna must be initialized
        return if (!ChipDnaMobile.isInitialized()) {
            if (initArgs.isNotEmpty()) {
                async { initialize(initArgs) }.await()
                Companion.getConnectedReader(
                    tapToPayConfig = tapToPayConfig,
                    isTapToPayReaderConnected = isTapToPayReaderConnected
                )
            } else {
                throw OmniGeneralException.uninitialized
            }
        } else {
            val reader = Companion.getConnectedReader(
                tapToPayConfig = tapToPayConfig,
                isTapToPayReaderConnected = isTapToPayReaderConnected
            )
            log("getConnectedReader returning: ${reader?.getName() ?: "null"}")
            reader
        }
    }

    override suspend fun isReadyToTakePayment(): Boolean {
        log("isReadyToTakePayment called")
        // ChipDna must be initialized
        if (!ChipDnaMobile.isInitialized()) {
            log("isReadyToTakePayment: ChipDNA not initialized")
            return false
        }

        try {
            log("isReadyToTakePayment: isTapToPayEnabled=${isTapToPayEnabled()}, isTapToPayReaderConnected=$isTapToPayReaderConnected")
            
            // For Tap to Pay, we just need SDK initialized and reader connected
            // Terminal status becomes enabled asynchronously, so we don't wait for it
            if (isTapToPayEnabled() && isTapToPayReaderConnected) {
                log("isReadyToTakePayment: Tap to Pay is ready (SDK initialized and reader connected)")
                return true
            }

            // For external readers, check device and terminal status
            val status = ChipDnaMobile.getInstance().getStatus(null)

            // For external readers, device must be connected
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

    override suspend fun performTransaction(request: TransactionRequest, readerType: com.fattmerchant.omni.data.ReaderType, signatureProvider: SignatureProviding?, transactionUpdateListener: TransactionUpdateListener?, userNotificationListener: UserNotificationListener?): TransactionResult {
        val paymentRequestParams = withTransactionRequest(request)

        // Determine which reader to use based on readerType parameter
        when (readerType) {
            com.fattmerchant.omni.data.ReaderType.TAP_TO_PAY -> {
                // Explicitly request Tap to Pay
                log("Explicit TAP_TO_PAY selected: Setting TransactionPOI to TapToMobile")
                paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
            }
            com.fattmerchant.omni.data.ReaderType.EXTERNAL_READER -> {
                // Explicitly request external reader (don't set TransactionPOI, uses default)
                log("Explicit EXTERNAL_READER selected: Using default (external reader)")
            }
            com.fattmerchant.omni.data.ReaderType.AUTO -> {
                // Auto mode: Check what's connected and make intelligent selection
                if (isTapToPayEnabled() && tapToPayConfig?.allowExternalReaders == true) {
                    // Hybrid mode: Check if external reader is connected
                    try {
                        val status = ChipDnaMobile.getInstance().getStatus(null)
                        val deviceStatusXml = status[ParameterKeys.DeviceStatus]
                        
                        val isExternalReaderConnected = deviceStatusXml?.let { xml ->
                            try {
                                val deviceStatus = ChipDnaMobileSerializer.deserializeDeviceStatus(xml)
                                deviceStatus.status == DeviceStatus.DeviceStatusEnum.DeviceStatusConnected
                            } catch (e: Exception) {
                                log("Error parsing device status: ${e.message}")
                                false
                            }
                        } ?: false

                        if (!isExternalReaderConnected) {
                            // No external reader connected - use Tap to Pay
                            log("AUTO mode (hybrid): No external reader connected, setting TransactionPOI to TapToMobile")
                            paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
                        } else {
                            log("AUTO mode (hybrid): External reader connected, using default (external reader)")
                        }
                    } catch (e: Exception) {
                        // If we can't determine device status, default to Tap to Pay in hybrid mode
                        log("Error checking device status, defaulting to Tap to Pay: ${e.message}")
                        paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
                    }
                } else if (isTapToPayEnabled()) {
                    // Tap to Pay only mode - always use Tap to Pay
                    log("AUTO mode (Tap only): Setting TransactionPOI to TapToMobile")
                    paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
                }
                // If Tap to Pay is disabled, use default (external reader)
            }
        }

        val result = suspendCancellableCoroutine { continuation ->

            val transactionListener = ChipDnaTransactionListener()
            transactionListener.onFinish = {
                ChipDnaMobile.getInstance().apply {
                    removeTransactionUpdateListener(transactionListener)
                    removeTransactionFinishedListener(transactionListener)
                    removeDeferredAuthorizationListener(transactionListener)
                    removeSignatureVerificationListener(transactionListener)
                    removeVoiceReferralListener(transactionListener)
                    removePartialApprovalListener(transactionListener)
                    removeForceAcceptanceListener(transactionListener)
                    removeVerifyIdListener(transactionListener)
                }
                continuation.resume(it)
            }

            transactionListener.signatureProvider = signatureProvider
            transactionListener.transactionUpdateListener = transactionUpdateListener
            transactionListener.userNotificationListener = userNotificationListener

            ChipDnaMobile.getInstance().apply {
                addUserNotificationListener(transactionListener)
                addApplicationSelectionListener(transactionListener)
                addTransactionUpdateListener(transactionListener)
                addTransactionFinishedListener(transactionListener)
                addDeferredAuthorizationListener(transactionListener)
                addSignatureVerificationListener(transactionListener)
                addVoiceReferralListener(transactionListener)
                addPartialApprovalListener(transactionListener)
                addForceAcceptanceListener(transactionListener)
                addVerifyIdListener(transactionListener)
            }

            // TODO: Handle the case where ChipDnaMobile didn't actually start the transaction
            // val response = ChipDnaMobile.getInstance().startTransaction(paymentRequestParams)
            ChipDnaMobile.getInstance().startTransaction(paymentRequestParams)
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
        // val addressZip = result[ParameterKeys.BillingZipCode]
        // val address1 = result[ParameterKeys.BillingAddress1]
        // val address2 = result[ParameterKeys.BillingAddress2]
        // val addressState = result[ParameterKeys.BillingState]
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

        // TODO: Handle errors
        // val response = ChipDnaMobile.getInstance().voidTransaction(voidRequestParams)
        ChipDnaMobile.getInstance().voidTransaction(voidRequestParams)

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
                // this.request = request
                success = true
                transactionType = "refund"
                amount = Amount(cents = amountCents)
            }
        } else {
            throw RefundTransactionException(result[ParameterKeys.ErrorDescription] ?: "Could not refund transaction")
        }
    }

    override suspend fun cancelCurrentTransaction(error: ((OmniException) -> Unit)?): Boolean {
        val result = ChipDnaMobile.getInstance().terminateTransaction(null)
        result?.let {
            if (result[ParameterKeys.Result] == ParameterValues.TRUE) {
                return true
            } else {
                val status = ChipDnaMobile.getInstance().getStatus(null)
                if (status[ParameterKeys.ChipDnaStatus] == "IDLE") {
                    error?.invoke(CancelCurrentTransactionException.NoTransactionToCancel)
                } else {
                    error?.invoke(CancelCurrentTransactionException.Unknown)
                }
            }
        }
        return false
    }

    private fun setCredentials(appId: String, apiKey: String): Parameters {
        // Use TEST environment when testMode is enabled, otherwise LIVE
        val environment = if (tapToPayConfig?.testMode == true) {
            log("Using TEST environment for Tap to Pay testing")
            ParameterValues.TestEnvironment
        } else {
            log("Using LIVE environment for production")
            ParameterValues.LiveEnvironment
        }
        
        val params = Parameters().apply {
            add(ParameterKeys.ApiKey, apiKey)
            add(ParameterKeys.Environment, environment)
            add(ParameterKeys.ApplicationIdentifier, appId)
            
            // Add certificate fingerprint for Tap to Pay if enabled
            if (tapToPayConfig?.enabled == true) {
                val fingerprint = CertificateUtils.getCertificateFingerprint(appContext)
                if (fingerprint != null) {
                    add(ParameterKeys.CertificateFingerprint, fingerprint)
                    log("Added CertificateFingerprint for Tap to Pay: ${fingerprint.take(20)}...")
                } else {
                    log("WARNING: Unable to extract certificate fingerprint for Tap to Pay")
                }
            }
        }
        
        log("Calling setProperties with ApiKey, Environment=$environment, ApplicationIdentifier")
        return ChipDnaMobile.getInstance().setProperties(params)
    }

    private fun deserializePinPads(pinPadsXml: String?): List<SelectablePinPad> {
        if (pinPadsXml == null) {
            return listOf()
        }

        val pinPadsList = mutableListOf<SelectablePinPad>()
        try {
            val pinPadsMap = ChipDnaMobileSerializer.deserializeAvailablePinPads(pinPadsXml)
            for (connectionType in pinPadsMap.keys) {
                pinPadsMap[connectionType]?.let { pinPads ->
                    for (pinPad in pinPads) {
                        if (!isKnownPinPad(pinPad)) { continue }
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
        parameters?.let { params ->
            // Log all configuration parameters for debugging
            val configUpdate = params[ParameterKeys.ConfigurationUpdate]
            val percentComplete = params["PercentageComplete"]
            val currentStage = params["CurrentStage"]
            val status = params["Status"]
            val description = params["Description"]
            
            log("Configuration Update: $configUpdate")
            percentComplete?.let { log("Progress: $it%") }
            currentStage?.let { log("Stage: $it") }
            status?.let { log("Status: $it") }
            description?.let { log("Description: $it") }
            
            // Check if terminal is now enabled after configuration
            if (configUpdate == "ConfigurationUpdateComplete") {
                val terminalStatus = ChipDnaMobile.getInstance().getStatus(null)
                val terminalStatusXml = terminalStatus[ParameterKeys.TerminalStatus]
                terminalStatusXml?.let {
                    val terminal = ChipDnaMobileSerializer.deserializeTerminalStatus(it)
                    if (terminal.isEnabled) {
                        log("Terminal is now enabled after configuration")
                        onTerminalReadyCallback?.invoke()
                        onTerminalReadyCallback = null
                    }
                }
            }
            
            // Update connection status based on configuration update
            configUpdate?.let {
                MobileReaderConnectionStatus.from(it)?.let { status ->
                    log("Configuration status changed to: $status")
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(status)
                }
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

    // Prevents other devices from connecting!
    private fun isKnownPinPad(pad: String): Boolean {
        val known = listOf("CHB", "IDTECH")
        for (prefix in known) {
            if (pad.uppercase().startsWith(prefix)) {
                return true
            }
        }
        return false
    }
    
    /**
     * Registers a RequestActivityListener for Tap to Pay NFC operations.
     * 
     * This MUST be called when using Tap to Pay functionality. The ChipDNA SDK requires
     * an activity context to display NFC prompts and handle contactless payments.
     * 
     * @param delegate The RequestActivityDelegate that provides the current Activity
     */
    fun registerRequestActivityListener(delegate: RequestActivityDelegate) {
        if (!ChipDnaMobile.isInitialized()) {
            log("Warning: ChipDNA not initialized. RequestActivityListener registration may fail.")
            return
        }
        
        requestActivityDelegate = delegate
        ChipDnaMobile.getInstance().addRequestActivityListener(delegate)
        log("RequestActivityListener registered for Tap to Pay")
    }
    
    /**
     * Unregisters the RequestActivityListener.
     * Should be called when the activity is destroyed to prevent memory leaks.
     */
    fun unregisterRequestActivityListener() {
        // Note: ChipDNA SDK doesn't provide a method to unregister listeners
        // Setting to null prevents further callbacks
        requestActivityDelegate = null
        log("RequestActivityListener unregistered")
    }
    
    /**
     * Returns true if Tap to Pay is enabled in the current configuration.
     */
    fun isTapToPayEnabled(): Boolean {
        return tapToPayConfig?.enabled == true
    }
    
    /**
     * Returns the current Tap to Pay configuration.
     */
    fun getTapToPayConfiguration(): TapToPayConfiguration? {
        return tapToPayConfig
    }
}
