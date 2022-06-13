package com.fattmerchant.android.mock

import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.MobileReaderDetails.NMIDetails
import com.fattmerchant.omni.data.models.MobileReaderDetails.AWCDetails
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionResult
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest

internal fun createMockReader(forIndex: Int): MobileReader {
    return object : MobileReader {
        override fun getName() = "Reader $forIndex"
        override fun getFirmwareVersion() = "FakeFirmwareVersion$forIndex"
        override fun getMake() = "FakeMakeR$forIndex"
        override fun getModel() = "FakeModelR$forIndex"
        override fun serialNumber() = "FakeSerialNumber$forIndex"
    }
}

internal final class MockDriver: MobileReaderDriver {

    override val source: String = "MOCKSOURCE"
    override var familiarSerialNumbers: MutableList<String> = mutableListOf()
    override var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null

    /// Set this to false to simulate a busy mobile reader
    var readyToTakePayment: Boolean = true

    var isInitialized: Boolean = true
    var shouldConnect: Boolean = true

    var currentlyConnectedReader: MobileReader? = null

    override suspend fun isReadyToTakePayment(): Boolean {
        return readyToTakePayment
    }

    override suspend fun isOmniRefundsSupported(): Boolean {
        return false
    }

    override suspend fun initialize(args: Map<String, Any>): Boolean {
        val nmiDetails: NMIDetails? = args["nmi"] as? NMIDetails
        val awcDetails: AWCDetails? = args["awc"] as? AWCDetails
        return nmiDetails?.securityKey.isNullOrBlank() || (awcDetails?.terminalId.isNullOrBlank() && awcDetails?.terminalSecret.isNullOrBlank())
    }

    override suspend fun isInitialized(): Boolean {
        return isInitialized
    }

    override suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader> {
        var listOfReaders: MutableList<MobileReader> = mutableListOf()
        val readersNeeded = args["readersNeeded"] as? Int ?: return listOfReaders
        for (i in 1 until readersNeeded) {
            val mockReader = createMockReader(i)
            listOfReaders.add(mockReader)
        }
        return listOfReaders
    }

    override suspend fun connectReader(reader: MobileReader): MobileReader? {
        if (!shouldConnect) { return null }
        reader.serialNumber()?.let {
            familiarSerialNumbers.add(it)
        }
        currentlyConnectedReader = reader
        return reader
    }

    override suspend fun disconnect(reader: MobileReader, error: (OmniException) -> Unit): Boolean {
        currentlyConnectedReader = null
        return true
    }

    override suspend fun getConnectedReader(): MobileReader? {
        return currentlyConnectedReader ?: null
    }

    override suspend fun performTransaction(
        request: TransactionRequest,
        signatureProvider: SignatureProviding?,
        transactionUpdateListener: TransactionUpdateListener?,
        userNotificationListener: UserNotificationListener?
    ): TransactionResult {
        return performTransactionRequest(request,signatureProvider,transactionUpdateListener,userNotificationListener)
    }

    private fun performTransactionRequest(
        request: TransactionRequest,
        signatureProvider: SignatureProviding?,
        transactionUpdateListener: TransactionUpdateListener?,
        userNotificationListener: UserNotificationListener?
    ): TransactionResult {
        val isInsufficient = request.bankAccount?.bankName == "fakeBank" && request.amount.dollars() == 1000.0
        val isDeclined = request.bankAccount?.bankRouting == "fakeRoute"
        var transactionResult = TransactionResult()
        transactionResult.request = request
        transactionResult.success = if (isDeclined) false else !isInsufficient
        transactionResult.maskedPan = "411111111234"
        transactionResult.cardHolderFirstName = "William"
        transactionResult.cardHolderLastName = "Holder"
        transactionResult.authCode = "abc123"
        transactionResult.transactionType = "charge"
        transactionResult.amount = request.amount
        transactionResult.cardType = "visa"
        transactionResult.userReference = "cdm-123123"
        (if (isDeclined) "Transaction declined" else if (isInsufficient) "Insufficient funds" else null).also { transactionResult.message = it }
        return transactionResult
    }

    override suspend fun cancelCurrentTransaction(error: ((OmniException) -> Unit)?): Boolean {
        return true
    }

    override suspend fun refundTransaction(
        transaction: Transaction,
        refundAmount: Amount?
    ): TransactionResult {
        return runRefund(transaction, refundAmount)
    }

    private fun runRefund(
        transaction: Transaction,
        refundAmount: Amount?
    ): TransactionResult {
        var transactionResult = TransactionResult()
        transactionResult.request = null
        transactionResult.success = true
        transactionResult.maskedPan = "411111111234"
        transactionResult.cardHolderFirstName = "William"
        transactionResult.cardHolderLastName = "Holder"
        transactionResult.authCode = "def456"
        transactionResult.transactionType = "refund"
        transactionResult.amount = Amount(5)
        transactionResult.cardType = "visa"
        transactionResult.userReference = "cdm-123123"
        transactionResult.transactionSource = null
        return transactionResult
    }

    override suspend fun capture(transaction: Transaction): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun voidTransaction(transaction: Transaction): TransactionResult {
        TODO("Not yet implemented")
    }

    override fun voidTransaction(
        transactionResult: TransactionResult,
        completion: (Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}
