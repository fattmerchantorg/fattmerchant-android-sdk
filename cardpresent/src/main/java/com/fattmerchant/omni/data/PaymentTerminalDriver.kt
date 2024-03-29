package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.MobileReaderDriver.InitializeMobileReaderDriverException
import com.fattmerchant.omni.data.MobileReaderDriver.PerformTransactionException
import com.fattmerchant.omni.data.models.Transaction

interface PaymentTerminalDriver {

    /** The place where the transaction took place. For example, "NMI" or "AWC" */
    val source: String

    /**
     * Attempts to perform the given transaction [request]
     *
     * @throws PerformTransactionException
     * @param request has all the information required to run a transaction
     * @param signatureProvider responsible for providing a signature should the transaction
     * require one
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
    @Throws(MobileReaderDriver.VoidTransactionException::class)
    suspend fun voidTransaction(transaction: Transaction): TransactionResult

    /**
     * Attempts to refund the given [transaction]
     *
     * @param transaction
     * @return the result of the operation
     */
    @Throws(MobileReaderDriver.RefundTransactionException::class)
    suspend fun refundTransaction(transaction: Transaction, refundAmount: Amount? = null): TransactionResult

    /**
     * Attempts to initialize the [MobileReaderDriver]
     *
     * @throws InitializeMobileReaderDriverException
     * @param args
     */
    @Throws(InitializeMobileReaderDriverException::class)
    suspend fun initialize(args: Map<String, Any>): Boolean
}
