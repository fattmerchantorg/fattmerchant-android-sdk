package com.fattmerchant.android.chipdna

import android.content.Context
import com.creditcall.chipdnamobile.ChipDnaMobile
import com.creditcall.chipdnamobile.ChipDnaMobileSerializer
import com.creditcall.chipdnamobile.DeviceStatus
import com.creditcall.chipdnamobile.IAvailablePinPadsListener
import com.creditcall.chipdnamobile.IConfigurationUpdateListener
import com.creditcall.chipdnamobile.IConnectAndConfigureFinishedListener
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
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionResult
import com.fattmerchant.omni.data.models.MobileReaderConnectionStatus
import com.fattmerchant.omni.data.models.MobileReaderDetails
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.usecase.CancelCurrentTransactionException
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

internal class ChipDnaDriver :
    CoroutineScope,
    MobileReaderDriver,
    IConfigurationUpdateListener,
    IDeviceUpdateListener {

    class ConnectReaderException(message: String? = null) :
        MobileReaderDriver.ConnectReaderException(mapDetailMessage(message)) {
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
        /** The make of the pin pad */
        internal final class PinPadManufacturer(val name: String) {
            companion object {
                val Miura = PinPadManufacturer("Miura")
                val BBPOS = PinPadManufacturer("BBPOS")
            }
        }

        /**
         * Attempts to get the connected mobile reader.
         *
         * @param chipDnaMobileStatus the result of [ChipDnaMobile.getStatus]. If none is provided, this
         * method will retrieve it
         * @return the connected [MobileReader], if found. Null otherwise
         */
        internal fun getConnectedReader(chipDnaMobileStatus: Parameters? = ChipDnaMobile.getInstance().getStatus(null)): MobileReader? {
            val deviceStatusXml = chipDnaMobileStatus[ParameterKeys.DeviceStatus] ?: return null
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

        return result[ParameterKeys.Result] == ParameterValues.TRUE
    }

    override suspend fun isOmniRefundsSupported(): Boolean {
        return true
    }

    override suspend fun isInitialized(): Boolean {
        return ChipDnaMobile.isInitialized()
    }

    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        val parameters = Parameters().apply {
            add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.TRUE)
        }
        ChipDnaMobile.getInstance().clearAllAvailablePinPadsListeners()

        val pinPads = suspendCancellableCoroutine<List<SelectablePinPad>> { cont ->
            val availablePinPadsListener: IAvailablePinPadsListener? = null
            ChipDnaMobile.getInstance().addAvailablePinPadsListener { params ->
                val availablePinPadsXml = params?.getValue(ParameterKeys.AvailablePinPads)
                val pinPads = deserializePinPads(availablePinPadsXml!!)
                availablePinPadsListener?.let { ChipDnaMobile.getInstance().removeAvailablePinPadsListener(it) }

                cont.resume(pinPads)
            }

            ChipDnaMobile.getInstance().getAvailablePinPads(parameters)
        }

        return pinPads.map {
            mapPinPadToMobileReader(it)
        }
    }

    override suspend fun connectReader(reader: MobileReader): MobileReader? {

        val requestParams = Parameters()
        requestParams.add(ParameterKeys.PinPadName, reader.getName())
        requestParams.add(ParameterKeys.PinPadConnectionType, ParameterValues.BluetoothConnectionType)

        ChipDnaMobile.getInstance().setProperties(requestParams)

        return suspendCancellableCoroutine { cont ->
            var connectAndConfigureListener: IConnectAndConfigureFinishedListener? = null
            connectAndConfigureListener = IConnectAndConfigureFinishedListener { params ->
                ChipDnaMobile.getInstance().removeConnectAndConfigureFinishedListener(connectAndConfigureListener)

                if (params[ParameterKeys.Result] == ParameterValues.TRUE) {
                    // Reader is connected. Add the serial number to the list of familiar ones
                    // And return the hydrated mobile reader
                    Companion.getConnectedReader()?.let { connectedReader ->
                        connectedReader.serialNumber()?.let { familiarSerialNumbers.add(it) }
                        cont.resume(connectedReader)
                    }
                    return@IConnectAndConfigureFinishedListener
                }

                val error = params[ParameterKeys.ErrorDescription]
                cont.resumeWithException(ConnectReaderException(error))
            }

            ChipDnaMobile.getInstance().addConnectAndConfigureFinishedListener(connectAndConfigureListener)
            ChipDnaMobile.getInstance().connectAndConfigure(requestParams)
        }
    }

    override suspend fun disconnect(reader: MobileReader, error: (OmniException) -> Unit): Boolean {
        ChipDnaMobile.dispose(null)
        mobileReaderConnectionStatusListener?.mobileReaderConnectionStatusUpdate(MobileReaderConnectionStatus.DISCONNECTED)
        initialize(initArgs)
        return true
    }

    override suspend fun getConnectedReader(): MobileReader? {
        // ChipDna must be initialized
        if (!ChipDnaMobile.isInitialized()) {
            throw OmniGeneralException.uninitialized
        } else {
            return Companion.getConnectedReader()
        }
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
            cardType = result[ParameterKeys.CardSchemeId]?.toLowerCase(Locale.ROOT)
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

    override suspend fun cancelCurrentTransaction(error: ((OmniException) -> Unit)?): Boolean {
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
        if (pinPadsXml == null) {
            return listOf()
        }

        val availablePinPadsList = ArrayList<SelectablePinPad>()

        try {
            val availablePinPadsHashMap = ChipDnaMobileSerializer.deserializeAvailablePinPads(pinPadsXml)
            for (connectionType in availablePinPadsHashMap.keys) {
                for (pinPad in availablePinPadsHashMap[connectionType]!!) {
                    availablePinPadsList.add(SelectablePinPad(pinPad, connectionType))
                }
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return availablePinPadsList
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
