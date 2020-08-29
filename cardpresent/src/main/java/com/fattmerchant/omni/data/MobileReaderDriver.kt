package com.fattmerchant.omni.data

import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.SignatureProviding
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.usecase.CancelCurrentTransactionException

internal interface MobileReaderDriver {

    class PerformTransactionException(message: String? = null) :
        OmniException("Could not perform transaction", message)

    class VoidTransactionException(message: String? = null) :
        OmniException("Could not void transaction", message)

    class RefundTransactionException(message: String? = null) :
        OmniException("Could not refund transaction", message)

    open class ConnectReaderException(message: String? = null) :
        OmniException("Could not connect mobile reader", message)

    class InitializeMobileReaderDriverException(message: String? = null) :
        OmniException("Could not initialize mobile reader driver", message)

    /** A list of serial numbers that this driver has previously connected to */
    var familiarSerialNumbers: MutableList<String>

    /** The place where the transaction took place. For example, "NMI" or "AWC" */
    val source: String

    var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener?

    /**
     * Whether or not the given [MobileReaderDriver] is ready to take payment
     */
    suspend fun isReadyToTakePayment(): Boolean

    /**
     * True when the Omni API can perform the refund
     *
     * Some `MobileReaderDriver`s, like NMI, support a deeper integration with Omni, such that Omni can facilitate the
     * void/refund. This allows the SDK to relieve itself of the responsibility of having to perform the refund directly
     * with the vendor (NMI), via the vendored SDK (ChipDNA).
     *
     * Other `MobileReaderDriver`s, like AWC, do not support this integration. For that reason, the SDK must perform
     * the void/refund directly with AWC via the AWC sdk.
     */
    suspend fun isOmniRefundsSupported(): Boolean

    /**
     * Attempts to initialize the [MobileReaderDriver]
     *
     * @throws InitializeMobileReaderDriverException
     * @param args
     */
    @Throws(InitializeMobileReaderDriverException::class)
    suspend fun initialize(args: Map<String, Any>): Boolean

    /**
     * Checks if the receiver has been initialized
     */
    suspend fun isInitialized(): Boolean

    /**
     * Searches for available [MobileReader]s
     *
     * @param args
     */
    suspend fun searchForReaders(args: Map<String, Any>): List<MobileReader>

    /**
     * Attempts to connect the given [reader]
     *
     * @param reader
     * @return the connected [MobileReader] or nil
     */
    suspend fun connectReader(reader: MobileReader): MobileReader?

    /**
     * Disconnects the given mobile reader
     *
     * @param reader the reader to disconnect
     * @param error a block to run if something goes wrong
     * @return true if the reader was disconnected
     */
    suspend fun disconnect(reader: MobileReader, error: (OmniException) -> Unit): Boolean

    /**
     * Gets the [MobileReader] that is currently connected and accessible via the receiver
     *
     * @return the connected [MobileReader] or null
     */
    suspend fun getConnectedReader(): MobileReader?

    /**
     * Attempts to perform the given transaction [request]
     *
     * @throws PerformTransactionException
     * @param request has all the information required to run a transaction
     * @param signatureProvider responsible for providing a signature should the transaction
     * require one
     * @param transactionUpdateListener gets notified of transaction updates
     * @return the result of the operation
     */
    @Throws(PerformTransactionException::class)
    suspend fun performTransaction(request: TransactionRequest, signatureProvider: SignatureProviding?, transactionUpdateListener: TransactionUpdateListener?): TransactionResult

    /**
     * Attempts to void the given [transaction]
     *
     * @param transaction
     * @return the result of the operation
     */
    @Throws(VoidTransactionException::class)
    suspend fun voidTransaction(transaction: Transaction): TransactionResult

    /**
     * Attempts to refund the given [transaction]
     *
     * @param transaction
     * @return the result of the operation
     */
    @Throws(RefundTransactionException::class)
    suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount? = null): TransactionResult

    /**
     * Attempts to cancel a current mobile reader [transaction]
     *
     * @param error
     * @return the result of the operation
     */
    @Throws(CancelCurrentTransactionException::class)
    suspend fun cancelCurrentTransaction(error: ((OmniException) -> Unit)?): Boolean
}