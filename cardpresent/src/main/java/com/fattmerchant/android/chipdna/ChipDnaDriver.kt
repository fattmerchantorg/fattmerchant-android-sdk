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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
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
    
    /** Application context for SDK operations */
    private var applicationContext: Context? = null
    
    /** Tap to Pay configuration */
    private var tapToPayConfig: TapToPayConfiguration? = null
    
    /** Activity delegate for NFC operations in Tap to Pay mode */
    private var requestActivityDelegate: RequestActivityDelegate? = null
    
    /** Tracks whether the TapToPayReader is currently connected */
    private var isTapToPayReaderConnected: Boolean = false

    /** Guards against duplicate connectAndConfigure calls */
    private val isConnectAndConfigureInProgress = AtomicBoolean(false)

    override var familiarSerialNumbers: MutableList<String> = mutableListOf()
    override val source: String = "NMI"
    override var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    private val logger = Logger.getLogger("ChipDNA")
    fun log(msg: String?) {
        val prefix = if (isTapToPayReaderConnected || tapToPayConfig?.enabled == true) "[TTM]" else "[ChipDNA]"
        logger.info("$prefix [${Thread.currentThread().name}] $msg")
    }

    /** Cached initialization args for SDK re-initialization after dispose() */
    private var initArgs: Map<String, Any> = mapOf()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    override suspend fun initialize(args: Map<String, Any>): Boolean {
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
        
        applicationContext = appContext
        tapToPayConfig = args["tapToPayConfig"] as? TapToPayConfiguration

        val params = Parameters().apply {
            add(ParameterKeys.Password, "password")
            add(ParameterKeys.AutoConfirm, ParameterValues.TRUE)
        }

        ChipDnaMobile.initialize(appContext, params)
        ChipDnaMobile.getInstance().addConfigurationUpdateListener(this)
        ChipDnaMobile.getInstance().addDeviceUpdateListener(this)

        securityKey = apiKey
        initArgs = args

        val result = setCredentials(appId, apiKey)
        
        if (result[ParameterKeys.Result] != ParameterValues.TRUE) {
            log("❌ setProperties failed: ${result[ParameterKeys.ErrorDescription]}")
            result[ParameterKeys.Errors]?.let { log("   Errors: $it") }
        } else {
            log("✅ Credentials configured successfully")
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
        
        if (isTapToPayEnabled()) {
            log("Adding Tap to Pay reader to available readers")
            readersList.add(TapToPayReader())
        }
        
        if (tapToPayConfig?.allowExternalReaders != false || !isTapToPayEnabled()) {
            val externalReaders = searchForExternalReaders()
            readersList.addAll(externalReaders)
        }
        
        return readersList
    }
    
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

        if (reader is TapToPayReader) {
            log("🎯 Connecting Tap to Pay reader...")
            log("Test mode: ${reader.testMode}")

            if (!isConnectAndConfigureInProgress.compareAndSet(false, true)) {
                log("⚠️ connectAndConfigure already in progress, skipping duplicate call")
                throw ConnectReaderException("Connection already in progress")
            }

            return suspendCancellableCoroutine { continuation ->
                log("Initiating connectAndConfigure...")
                mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(
                    MobileReaderConnectionStatus.CONNECTING
                )
                doConnectAndConfigure(reader, continuation)
            }
        }
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
        val connectParams = Parameters().apply {
            add(ParameterKeys.TapToMobilePOI, ParameterValues.TRUE)
            add(ParameterKeys.PaymentDevicePOI, ParameterValues.FALSE)
        }
        
        log("📊 Connecting Tap to Pay: TapToMobilePOI=TRUE, PaymentDevicePOI=FALSE")
        
        val timeoutJob = launch {
            delay(120000)
            log("⏱️ Connection timeout after 2 minutes")
            isConnectAndConfigureInProgress.set(false)
            continuation.resumeWith(Result.failure(
                ConnectReaderException("Connection timeout: ChipDNA Mobile did not respond within 2 minutes")
            ))
        }
        
        if (!ChipDnaMobile.isInitialized()) {
            log("❌ ChipDNA Mobile is not initialized")
            timeoutJob.cancel()
            isConnectAndConfigureInProgress.set(false)
            continuation.resumeWith(Result.failure(
                ConnectReaderException("ChipDNA Mobile SDK not initialized")
            ))
            return
        }
        
        val preConnectStatus = ChipDnaMobile.getInstance().getStatus(null)
        log("📊 SDK Status: ${preConnectStatus[ParameterKeys.Result]}, POSGUID: ${preConnectStatus[ParameterKeys.POSGUID]}, TAPPOIIDENTIFIER: ${preConnectStatus[ParameterKeys.TapToMobilePOIIdentifier]}")
        preConnectStatus[ParameterKeys.Errors]?.let { log("   Errors: $it") }
        
        ChipDnaMobile.getInstance().apply {
            clearAllConnectAndConfigureFinishedListeners()
            
            addConnectAndConfigureFinishedListener { callbackParams ->
                val result = callbackParams[ParameterKeys.Result]
                val errorCode = callbackParams["ErrorCode"]
                val errorDescription = callbackParams[ParameterKeys.ErrorDescription]
                val errors = callbackParams[ParameterKeys.Errors]
                
                timeoutJob.cancel()
                
                log("✅ connectAndConfigure callback received")
                log("Result: $result")
                log("Errors: $errors")
                log("ErrorCode: $errorCode")
                log("ErrorDescription: $errorDescription")
                
                // Log all known Tap to Pay error codes from NMI documentation
                val tapToPayErrorKeys = listOf(
                    "TapToMobileNotSupported",
                    "NoPoiSelected", 
                    "LocationPermissionsNotGranted",
                    "ApplicationUpdateRequired",
                    "CountryCodeInvalid",
                    "AttestationFailed",
                    "CurrentCountryNotAllowed",
                    "NoLocationFound",
                    "DeveloperOptionsEnabled",
                    "USBCableConnectedOrBluetoothEnabled",
                    "CustomROMDetected",
                    "EmulatorFound",
                    "ShowTouchesEnabled"
                )
                
                tapToPayErrorKeys.forEach { key ->
                    callbackParams[key]?.let { value ->
                        log("   ⚠️ NMI ERROR: $key = $value")
                    }
                }
                
                if (result == ParameterValues.TRUE) {
                    log("✅ Configuration successful")
                    logDeviceIdentifiers()
                    
                    isTapToPayReaderConnected = true
                    isConnectAndConfigureInProgress.set(false)
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(
                        MobileReaderConnectionStatus.CONNECTED
                    )
                    continuation.resumeWith(Result.success(reader))
                } else {
                    val userMessage = mapChipDnaErrorToUserMessage(errorCode, errorDescription)
                    log("❌ Configuration failed: $userMessage")
                    log("Error code: $errorCode")
                    log("Error description: $errorDescription")
                    
                    val postFailStatus = ChipDnaMobile.getInstance().getStatus(null)
                    val statusInfo = "SDK Status: ${postFailStatus[ParameterKeys.Result]}, POSGUID: ${postFailStatus[ParameterKeys.POSGUID]}, TAPPOIIDENTIFIER: ${postFailStatus[ParameterKeys.TapToMobilePOIIdentifier]}"
                    log(statusInfo)
                    
                    isConnectAndConfigureInProgress.set(false)
                    continuation.resumeWith(Result.failure(ConnectReaderException("$userMessage | $statusInfo")))
                }
            }
        }.connectAndConfigure(connectParams)
    }
    
    override suspend fun disconnect(reader: MobileReader?, error: (OmniException) -> Unit): Boolean {
        val retryLimit = 3
        val retryTimes = arrayOf(500L, 1000L, 2000L)
        var retryCount = 0

        do {
            val result = ChipDnaMobile.dispose(null)
            if (result[ParameterKeys.Result] == ParameterValues.TRUE) {
                isTapToPayReaderConnected = false
                isConnectAndConfigureInProgress.set(false)
                mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(MobileReaderConnectionStatus.DISCONNECTED)
                return true
            }

            async { delay(retryTimes[retryCount]) }.await()
            retryCount++
        } while (retryCount < retryLimit)

        return false
    }

    override suspend fun getConnectedReader(): MobileReader? {
        log("getConnectedReader called: isTapToPayReaderConnected=$isTapToPayReaderConnected")
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
        if (!ChipDnaMobile.isInitialized()) {
            log("ChipDNA not initialized")
            return false
        }

        try {
            log("isTapToPayEnabled=${isTapToPayEnabled()}, isTapToPayReaderConnected=$isTapToPayReaderConnected")
            
            if (isTapToPayEnabled() && isTapToPayReaderConnected) {
                log("Tap to Pay ready")
                return true
            }

            val status = ChipDnaMobile.getInstance().getStatus(null)

            val deviceStatusXml = status[ParameterKeys.DeviceStatus] ?: return false
            val deviceStatus = ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)
            if (deviceStatus.status != DeviceStatus.DeviceStatusEnum.DeviceStatusConnected) {
                return false
            }

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

        when (readerType) {
            com.fattmerchant.omni.data.ReaderType.TAP_TO_PAY -> {
                log("TAP_TO_PAY: Setting TransactionPOI to TapToMobile")
                paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
            }
            com.fattmerchant.omni.data.ReaderType.EXTERNAL_READER -> {
                log("EXTERNAL_READER: Using default")
            }
            com.fattmerchant.omni.data.ReaderType.AUTO -> {
                if (isTapToPayEnabled() && tapToPayConfig?.allowExternalReaders == true) {
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
                            log("AUTO (hybrid): Using Tap to Pay")
                            paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
                        } else {
                            log("AUTO (hybrid): Using external reader")
                        }
                    } catch (e: Exception) {
                        log("Error checking device status, defaulting to Tap to Pay: ${e.message}")
                        paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
                    }
                } else if (isTapToPayEnabled()) {
                    log("AUTO: Using Tap to Pay")
                    paymentRequestParams.add(ParameterKeys.TransactionPOI, ParameterValues.TapToMobile)
                }
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

            ChipDnaMobile.getInstance().startTransaction(paymentRequestParams)
        }

        if (result.containsKey(ParameterKeys.Errors)) {
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

        if (result[ParameterKeys.TransactionResult] == ParameterValues.Declined) {
            when {
                result[ParameterKeys.Errors]?.contains("GatewayRejectedTransaction") == true -> {
                    throw PerformTransactionException("Gateway rejected transaction")
                }
            }
        }

        val firstName = result[ParameterKeys.CardHolderFirstName]
        val lastName = result[ParameterKeys.CardHolderLastName]
        var ccExpiration: String? = null

        val receiptData = ChipDnaMobileSerializer.deserializeReceiptData(result[ParameterKeys.ReceiptData])

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
        val envMode = if (tapToPayConfig?.testMode == true) "TEST" else "LIVE"
        val environment = if (tapToPayConfig?.testMode == true) {
            ParameterValues.TestEnvironment
        } else {
            ParameterValues.LiveEnvironment
        }
        
        val params = Parameters().apply {
            add(ParameterKeys.ApiKey, apiKey)
            add(ParameterKeys.Environment, environment)
            add(ParameterKeys.ApplicationIdentifier, appId)
            
            tapToPayConfig?.let { config ->
                if (config.enabled) {
                    val fingerprint = config.certificateFingerprint
                        ?: (applicationContext?.let { CertificateUtils.getCertificateFingerprint(it) })
                    
                    if (fingerprint != null) {
                        add(ParameterKeys.CertificateFingerprint, fingerprint)
                    } else {
                        log("⚠️ Unable to extract certificate fingerprint")
                    }
                }
            }
        }
        
        log("🔐 Setting credentials (Environment: $envMode)")
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
            val configUpdate = params[ParameterKeys.ConfigurationUpdate]
            
            // Handle Tap to Pay specific configuration events
            when (configUpdate) {
                "CheckingTapToMobileConfig" -> {
                    log("🔍 Checking Tap to Pay configuration")
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(
                        MobileReaderConnectionStatus.CHECKING_TAP_TO_MOBILE_CONFIG
                    )
                }
                "UpdatingTapToMobileConfig" -> {
                    val percentComplete = params["PercentageComplete"]
                    log("⚙️ Updating Tap to Pay configuration ${percentComplete?.let { "($it%)" } ?: ""}")
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(
                        MobileReaderConnectionStatus.UPDATING_TAP_TO_MOBILE_CONFIG
                    )
                }
                "ConfigurationUpdateComplete" -> {
                    log("✅ Configuration update complete")
                }
                "CONNECT_AND_CONFIGURE_STARTED",
                "REGISTERING" -> {
                    // Log these common events only once without emoji
                    log("Configuration: $configUpdate")
                }
                else -> {
                    // Log other updates with any error details
                    val errors = params[ParameterKeys.Errors]
                    val errorDesc = params[ParameterKeys.ErrorDescription]
                    if (errors != null || errorDesc != null) {
                        log("Configuration: $configUpdate [Errors: $errors, Desc: $errorDesc]")
                    } else {
                        log("Configuration: $configUpdate")
                    }
                }
            }
            
            configUpdate?.let {
                MobileReaderConnectionStatus.from(it)?.let { status ->
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(status)
                }
            }
        }
    }

    override fun onDeviceUpdate(parameters: Parameters?) {
        log("🔔 onDeviceUpdate")
        
        parameters?.let { params ->
            val debugKeys = listOf(
                ParameterKeys.Errors,
                ParameterKeys.Result,
                "ErrorCode",
                ParameterKeys.ErrorDescription
            )
            debugKeys.forEach { key ->
                params[key]?.let { value ->
                    log("  $key: $value")
                }
            }
        }
        
        parameters[ParameterKeys.DeviceStatusUpdate]?.let { deviceStatusXml ->
            log("Device Status XML received: $deviceStatusXml")
            ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)?.let { deviceStatus ->
                log("Device Status parsed: ${deviceStatus.status}")
                MobileReaderConnectionStatus.from(deviceStatus.status)?.let {
                    log("Notifying connection status: $it")
                    mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(it)
                }
            }
        }
    }
    
    private fun logDeviceIdentifiers() {
        val status = ChipDnaMobile.getInstance().getStatus(null)
        
        status[ParameterKeys.POSGUID]?.let {
            log("📱 POS GUID: $it")
        }
        
        status[ParameterKeys.TapToMobilePOIIdentifier]?.let {
            log("📱 Tap to Pay POI Identifier: $it")
        }
    }
    
    private fun mapChipDnaErrorToUserMessage(errorCode: String?, errorDescription: String?): String {
        return when (errorCode) {
            // Tap to Pay Specific Errors - Connect And Configure
            "TapToMobileNotSupported" -> "This device does not support Tap to Pay"
            "NoPoiSelected" -> "No payment method was selected for configuration"
            "LocationPermissionsNotGranted" -> "Location permissions are required for Tap to Pay"
            "ApplicationUpdateRequired" -> "SDK update required. Please update the app"
            "CountryCodeInvalid" -> "Invalid country code for Tap to Pay"
            "AttestationFailed" -> "Security attestation failed. Cannot use Tap to Pay"
            "CurrentCountryNotAllowed" -> "Tap to Pay is not available in your current location"
            "NoLocationFound" -> "Unable to determine location. Check location permissions"
            "DeveloperOptionsEnabled" -> "Developer options must be disabled for Tap to Pay"
            "USBCableConnectedOrBluetoothEnabled" -> "Disconnect USB and disable Bluetooth for Tap to Pay"
            "CustomROMDetected" -> "Custom ROM detected. Tap to Pay requires stock ROM"
            "EmulatorFound" -> "Tap to Pay cannot run on an emulator"
            "ShowTouchesEnabled" -> "Disable 'Show touches' in Developer options"
            
            // Transaction Errors - Start Transaction
            "TransactionPOINotConnected" -> "Payment method not connected"
            "TransactionPOIInvalid" -> "Invalid payment method selected"
            "AutoConfirmRequired" -> "Transaction must be auto-confirmed for Tap to Pay"
            "TipAmountInvalid" -> "Invalid tip amount format"
            "TipAmountNotAllowed" -> "Tipping not supported for this payment method"
            "MerchantTippingNotSupported" -> "Merchant tipping not supported"
            "RequestActivityListenerRequired" -> "Activity listener required for Tap to Pay"
            "InvalidActivity" -> "Activity is invalid, finishing, or destroyed"
            
            // Transaction Finished Errors
            "MerchantTerminatedTransaction" -> "Transaction was cancelled"
            "TapToMobileSessionClosed" -> "Tap to Pay session is no longer available"
            "NfcDisabled" -> "NFC must be enabled for Tap to Pay"
            "NoPatternOrPinSet" -> "Device lock screen (PIN/pattern) must be set"
            "CameraUsed" -> "Camera is in use by another app"
            "MicrophoneUsed" -> "Microphone is in use by another app"
            
            // Connection Errors
            "ConnectionClosed" -> "Connection closed"
            "BluetoothNotEnabled" -> "Bluetooth not enabled"
            
            else -> errorDescription ?: "Unknown error: $errorCode"
        }
    }

    private fun isKnownPinPad(pad: String): Boolean {
        val known = listOf("CHB", "IDTECH")
        for (prefix in known) {
            if (pad.uppercase().startsWith(prefix)) {
                return true
            }
        }
        return false
    }
    
    /** Registers a RequestActivityListener for Tap to Pay NFC operations */
    fun registerRequestActivityListener(delegate: RequestActivityDelegate) {
        if (!ChipDnaMobile.isInitialized()) {
            log("Warning: ChipDNA not initialized. RequestActivityListener registration may fail.")
            return
        }
        
        requestActivityDelegate = delegate
        ChipDnaMobile.getInstance().addRequestActivityListener(delegate)
        log("RequestActivityListener registered for Tap to Pay")
    }
    
    fun unregisterRequestActivityListener() {
        requestActivityDelegate = null
        log("RequestActivityListener unregistered")
    }
    
    fun isTapToPayEnabled(): Boolean {
        return tapToPayConfig?.enabled == true
    }
    
    fun getTapToPayConfiguration(): TapToPayConfiguration? {
        return tapToPayConfig
    }
}
