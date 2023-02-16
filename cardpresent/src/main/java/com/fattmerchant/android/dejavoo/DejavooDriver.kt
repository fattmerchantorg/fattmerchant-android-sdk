package com.fattmerchant.android.dejavoo

import android.content.Context
import com.dvmms.dejapay.IRequestCallback
import com.dvmms.dejapay.exception.DejavooThrowable
import com.dvmms.dejapay.models.*
import com.dvmms.dejapay.models.DejavooTransactionRequest
import com.dvmms.dejapay.models.DejavooTransactionResponse
import com.dvmms.dejapay.terminals.InternalTerminal
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.DejavooTerminalCredentials
import com.fattmerchant.omni.data.models.PaymentType
import com.fattmerchant.omni.data.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class DejavooDriver : CoroutineScope, PaymentTerminalDriver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    override val source: String = "terminalservice.dejavoo"

    lateinit var appContext: Context
    lateinit var authenticationKey: String
    lateinit var tpn: String
    lateinit var registerId: String
    lateinit var appName: String

    override suspend fun initialize(args: Map<String, Any>): Boolean {
        val appContext = args["appContext"] as? Context
            ?: throw MobileReaderDriver.InitializeMobileReaderDriverException("appContext not found")
        this.appContext = appContext

        this.appName = args["appId"] as? String
            ?: throw MobileReaderDriver.InitializeMobileReaderDriverException("appId not found")

        val creds = args["dejavoo"] as? DejavooTerminalCredentials
            ?: throw MobileReaderDriver.InitializeMobileReaderDriverException("Dejavoo credentials not found")

        this.authenticationKey = creds.key
        this.tpn = creds.tpn
        this.registerId = creds.registerId

        return true
    }

    override suspend fun performTransaction(
        request: TransactionRequest
    ): TransactionResult {

        return suspendCancellableCoroutine { cancellableContinuation ->
            val dejavooRequest = DejavooTransactionRequest()

            dejavooRequest.authenticationKey = authenticationKey
            dejavooRequest.tpn = tpn
            dejavooRequest.registerId = registerId
            dejavooRequest.paymentType = when (request.paymentType) {
                PaymentType.DEBIT -> DejavooPaymentType.Debit
                else -> DejavooPaymentType.Credit
            }
            dejavooRequest.transactionType = DejavooTransactionType.Sale
            dejavooRequest.referenceId = request.transactionId ?: UUID.randomUUID().toString()
            dejavooRequest.isRepeatRequest = false
            dejavooRequest.setAmount(request.amount.dollars())
            dejavooRequest.isSignatureCapable = false
            dejavooRequest.invoiceNumber = UUID.randomUUID().toString()
            dejavooRequest.receiptType = DejavooTransactionRequest.ReceiptType.Both;
            dejavooRequest.printReceipt = DejavooTransactionRequest.ReceiptType.Both;
            dejavooRequest.clerkId = 0

            InternalTerminal("").commitTransaction(
                appContext, dejavooRequest, object : IRequestCallback<DejavooTransactionResponse> {
                    override fun onResponse(response: DejavooTransactionResponse) {
                        val auth = response.authenticationCode
                        val extData = DejavooResponseExtData(appName, response.extData)

                        val firstFour = extData.acntFirst4
                        val lastFour = extData.acntLast4
                        val pan = "$firstFour********$lastFour"
                        val names = extData.cardHolder.split("/").reversed()
                        var firstName = names.first()
                        var lastName = names.last()
                        val expiry = extData.data["ExpDate"] ?: "1299"

                        if (firstName.isBlank()) {
                            firstName = "Contactless"
                        }

                        if (lastName.isBlank()) {
                            lastName = "Customer"
                        }

                        val transactionResult = TransactionResult().apply {
                            authCode = auth
                            success =
                                response.resultCode == DejavooTransactionResponse.ResultCode.Succeded
                            maskedPan = pan
                            amount = Amount(dollars = extData.amount)
                            cardType = extData.cardTypeString.lowercase()
                            message = response.message
                            cardHolderFirstName = firstName
                            cardHolderLastName = lastName
                            cardExpiration = expiry
                            source = this@DejavooDriver.source
                            transactionSource = extData.entryTypeString
                            transactionMeta = mutableMapOf(
                                "RegisterId" to response.registerId,
                                "AuthCode" to response.authenticationCode,
                                "PNRef" to response.pnReference,
                                "RespMSG" to response.responseMessage,
                                "SN" to response.serialNumber,
                                "referenceId" to response.referenceId
                            )

                            // gatewayResponse = DejavooDriverUtils.gatewayResponse(response)
                            this.request = request
                        }

                        cancellableContinuation.resume(transactionResult)
                    }

                    override fun onError(throwable: DejavooThrowable) {
                        //TODO handle error
                    }
                }
            )

        }
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        var referenceId = (transaction.meta as? Map<*, *>)?.get("referenceId") as? String

        return suspendCancellableCoroutine { cancellableContinuation ->
            val dejavooRequest = DejavooTransactionRequest()
            dejavooRequest.authenticationKey = authenticationKey
            dejavooRequest.tpn = tpn
            dejavooRequest.registerId = registerId
            dejavooRequest.paymentType = DejavooPaymentType.Credit
            dejavooRequest.transactionType = DejavooTransactionType.Void
            dejavooRequest.referenceId = referenceId
            dejavooRequest.setAmount(transaction.total?.toDoubleOrNull() ?: 0.0)

            InternalTerminal("").commitTransaction(
                appContext, dejavooRequest, object : IRequestCallback<DejavooTransactionResponse> {
                    override fun onResponse(response: DejavooTransactionResponse) {
                        val auth = response.authenticationCode
                        val extData = DejavooResponseExtData(appName, response.extData)

                        val firstFour = extData.acntFirst4
                        val lastFour = extData.acntLast4
                        val pan = "$firstFour********$lastFour"
                        val names = extData.cardHolder.split("/").reversed()
                        var firstName = names.first()
                        var lastName = names.last()
                        val expiry = extData.data["ExpDate"] ?: "1299"

                        response.referenceId // this seems like the transaction ref id to use for voiding/refunding

                        if (firstName.isBlank()) {
                            firstName = "Contactless"
                        }

                        if (lastName.isBlank()) {
                            lastName = "Customer"
                        }

                        val transactionResult = TransactionResult().apply {
                            authCode = auth
                            success =
                                response.resultCode == DejavooTransactionResponse.ResultCode.Succeded
                            maskedPan = pan
                            amount = Amount(dollars = extData.amount)
                            cardType = extData.cardTypeString.lowercase()
                            message = response.message
                            cardHolderFirstName = firstName
                            cardHolderLastName = lastName
                            cardExpiration = expiry
                            source = this@DejavooDriver.source
                            transactionSource = extData.entryTypeString
                            // gatewayResponse = DejavooDriverUtils.gatewayResponse(response)
                            transactionMeta = mutableMapOf(
                                "RegisterId" to response.registerId,
                                "AuthCode" to response.authenticationCode,
                                "PNRef" to response.pnReference,
                                "RespMSG" to response.responseMessage,
                                "SN" to response.serialNumber,
                            )
                            transactionType = "void"
                            this.request = request
                        }

                        cancellableContinuation.resume(transactionResult)
                    }

                    override fun onError(throwable: DejavooThrowable) {

                    }
                }
            )

        }
    }


    override suspend fun refundTransaction(
        transaction: Transaction,
        refundAmount: Amount?
    ): TransactionResult {

        val referenceId = UUID.randomUUID().toString()

        return suspendCancellableCoroutine { cancellableContinuation ->
            val dejavooRequest = DejavooTransactionRequest()
            dejavooRequest.authenticationKey = authenticationKey
            dejavooRequest.tpn = tpn
            dejavooRequest.registerId = registerId
            dejavooRequest.paymentType = DejavooPaymentType.Credit
            dejavooRequest.transactionType = DejavooTransactionType.Return
            dejavooRequest.referenceId = referenceId
            dejavooRequest.setAmount(refundAmount?.dollars() ?: transaction.total?.toDoubleOrNull() ?: 0.0)

            InternalTerminal("").commitTransaction(
                appContext, dejavooRequest, object : IRequestCallback<DejavooTransactionResponse> {
                    override fun onResponse(response: DejavooTransactionResponse) {
                        val auth = response.authenticationCode
                        val extData = DejavooResponseExtData(appName, response.extData)

                        val firstFour = extData.acntFirst4
                        val lastFour = extData.acntLast4
                        val pan = "$firstFour********$lastFour"
                        val names = extData.cardHolder.split("/").reversed()
                        var firstName = names.first()
                        var lastName = names.last()
                        val expiry = extData.data["ExpDate"] ?: "1299"


                        if (firstName.isBlank()) {
                            firstName = "Contactless"
                        }

                        if (lastName.isBlank()) {
                            lastName = "Customer"
                        }

                        val transactionResult = TransactionResult().apply {
                            authCode = auth
                            success =
                                response.resultCode == DejavooTransactionResponse.ResultCode.Succeded
                            maskedPan = pan
                            amount = Amount(dollars = extData.amount)
                            cardType = extData.cardTypeString.lowercase()
                            message = response.message
                            cardHolderFirstName = firstName
                            cardHolderLastName = lastName
                            cardExpiration = expiry
                            source = this@DejavooDriver.source
                            transactionSource = extData.entryTypeString
                            // gatewayResponse = DejavooDriverUtils.gatewayResponse(response)
                            transactionMeta = mutableMapOf(
                                "RegisterId" to response.registerId,
                                "AuthCode" to response.authenticationCode,
                                "PNRef" to response.pnReference,
                                "RespMSG" to response.responseMessage,
                                "SN" to response.serialNumber,
                            )
                            transactionType = "refund"
                            this.request = request
                        }

                        cancellableContinuation.resume(transactionResult)
                    }

                    override fun onError(throwable: DejavooThrowable) {

                    }
                }
            )

        }
    }

}