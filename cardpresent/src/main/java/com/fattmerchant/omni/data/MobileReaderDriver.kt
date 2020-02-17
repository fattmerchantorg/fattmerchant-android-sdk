package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction

interface MobileReaderDriver {

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

    /**
     * Whether or not the given [MobileReaderDriver] is ready to take payment
     */
    suspend fun isReadyToTakePayment(): Boolean

    /**
     * Attempts to initialize the [MobileReaderDriver]
     *
     * @throws InitializeMobileReaderDriverException
     * @param args
     */
    @Throws(InitializeMobileReaderDriverException::class)
    suspend fun initialize(args: Map<String, Any>): Boolean

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
     */
    suspend fun connectReader(reader: MobileReader): Boolean

    /**
     * Attempts to perform the given transaction [request]
     *
     * @throws PerformTransactionException
     * @param request
     * @return the result of the operation
     */
    @Throws(PerformTransactionException::class)
    suspend fun performTransaction(request: TransactionRequest): TransactionResult

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
}