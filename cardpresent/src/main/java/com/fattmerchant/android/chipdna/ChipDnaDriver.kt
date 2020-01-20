package com.fattmerchant.android.chipdna

import android.content.Context
import com.creditcall.chipdnamobile.*
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.Merchant
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.MobileReaderDriver.*
import kotlinx.coroutines.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class ChipDnaDriver : CoroutineScope, MobileReaderDriver {

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

    inner class SelectablePinPad(var name: String, var connectionType: String)

    val log = Logger.getLogger("ChipDNA")
    fun log(msg: String?) {
        log.info("[${Thread.currentThread().name}] $msg")
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    override suspend fun initialize(args: Map<String, Any>): Boolean {
        // Make sure we have all the necessary data
        val appContext = args["appContext"] as? Context
            ?: throw InitializeMobileReaderDriverException("appContext not found")

        val appId = args["appId"] as? String
            ?: throw InitializeMobileReaderDriverException("appId not found")

        val merchant = args["merchant"] as? Merchant
            ?: throw InitializeMobileReaderDriverException("merchant not found")

        val apiKey = merchant.emvPassword()
            ?: throw InitializeMobileReaderDriverException("emvTerminalSecret not found")

        val params = Parameters().apply {
            add(ParameterKeys.Password, "password")
            add(ParameterKeys.AutoConfirm, ParameterValues.TRUE)
        }

        // Init
        ChipDnaMobile.initialize(appContext, params)

        // Set credentials
        val result = setCredentials(appId, apiKey)
        if (result[ParameterKeys.Result] != ParameterValues.TRUE) {
            throw InitializeMobileReaderDriverException("Invalid credentials for reader")
        }

        return result[ParameterKeys.Result] == ParameterValues.TRUE
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

    override suspend fun connectReader(reader: MobileReader): Boolean {

        val requestParams = Parameters()
        requestParams.add(ParameterKeys.PinPadName, reader.getName())
        requestParams.add(ParameterKeys.PinPadConnectionType, ParameterValues.BluetoothConnectionType)

        ChipDnaMobile.getInstance().setProperties(requestParams)

        return suspendCancellableCoroutine { cont ->
            var connectAndConfigureListener: IConnectAndConfigureFinishedListener? = null
            connectAndConfigureListener = IConnectAndConfigureFinishedListener { params ->
                ChipDnaMobile.getInstance().removeConnectAndConfigureFinishedListener(connectAndConfigureListener)

                if (params[ParameterKeys.Result] == ParameterValues.TRUE) {
                    cont.resume(true)
                    return@IConnectAndConfigureFinishedListener
                }

                val error = params[ParameterKeys.Error]
                throw ConnectReaderException(error)
            }

            ChipDnaMobile.getInstance().addConnectAndConfigureFinishedListener(connectAndConfigureListener)
            ChipDnaMobile.getInstance().connectAndConfigure(requestParams)
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

    override suspend fun performTransaction(request: TransactionRequest): TransactionResult {
        val paymentRequestParams = mapTransactionRequestToParams(request)

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

        return TransactionResult().apply {
            this.request = request
            authCode = result[ParameterKeys.AuthCode]
            maskedPan = result[ParameterKeys.MaskedPan] ?: ""
            userReference = result[ParameterKeys.UserReference]
            cardHolderFirstName = firstName
            cardHolderLastName = lastName
            cardType = result[ParameterKeys.CardSchemeId]?.toLowerCase()
            success = result[ParameterKeys.TransactionResult] == ParameterValues.Approved
        }
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        val ref = extractUserReference(transaction)

        val voidRequestParams = Parameters().apply {
            add(ParameterKeys.UserReference, ref)
        }

        var response = ChipDnaMobile.getInstance().voidTransaction(voidRequestParams)

        // TODO: Handle errors

        return TransactionResult()
    }

    override suspend fun refundTransaction(transaction: Transaction): TransactionResult {
        // Prepare Parameters for refunding
        val ref = extractUserReference(transaction)
        val amountCents = transaction.total?.toFloat()?.times(100)?.toInt() ?: 0
        val refundRequestParams = Parameters().apply {
            add(ParameterKeys.UserReference, generateUserReference())
            add(ParameterKeys.SaleReference, ref)
            add(ParameterKeys.Amount, amountCents)
            add(ParameterKeys.Currency, "USD")
        }

        val result = ChipDnaMobile.getInstance().linkedRefundTransaction(refundRequestParams)

        return if (result[ParameterKeys.TransactionResult] == ParameterValues.Approved) {
            TransactionResult().apply {
                this.request = request
                success = true
                transactionType = "refund"
            }
        } else {
            throw RefundTransactionException(result[ParameterKeys.Error] ?: "Could not refund transaction")
        }
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

    companion object {

        /**
         * Gets the User Reference from the given [Transaction]
         *
         * @param transaction
         * @return a string containing the user reference or null if not found
         */
        private fun extractUserReference(transaction: Transaction): String? =
            (transaction.meta as? Map<*, *>)?.get("nmiUserRef") as? String

        /**
         * Generates a user reference for chipDNA transactions
         *
         * @return String containing the generated user reference
         */
        private fun generateUserReference(): String =
            String.format("CDM-%s", SimpleDateFormat("yy-MM-dd-HH.mm.ss", Locale.US).format(Date()))

        /**
         * Converts a [TransactionRequest] into a [Parameters] object that ChipDNA understands
         *
         * @param request
         */
        private fun mapTransactionRequestToParams(request: TransactionRequest) = Parameters().apply {
            add(ParameterKeys.Amount, request.amount.centsString())
            add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual)
            add(ParameterKeys.Currency, "USD")
            add(ParameterKeys.UserReference, generateUserReference())
            add(ParameterKeys.PaymentMethod, ParameterValues.Card)
            add(ParameterKeys.AutoConfirm, ParameterValues.TRUE)
            add(ParameterKeys.TransactionType, ParameterValues.Sale)
        }
    }
}