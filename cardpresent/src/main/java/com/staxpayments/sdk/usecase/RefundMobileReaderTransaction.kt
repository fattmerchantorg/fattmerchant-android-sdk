package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.TransactionResult
import com.staxpayments.sdk.data.models.Transaction
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
import com.staxpayments.sdk.data.repository.TransactionRepository
import com.staxpayments.sdk.networking.StaxApi
import kotlinx.coroutines.coroutineScope

/**
 * Refunds the given [Transaction] and records the refund in Stax
 *
 * @property mobileReaderDriverRepository
 * @property transactionRepository
 * @property transaction
 */
internal class RefundMobileReaderTransaction(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val transactionRepository: TransactionRepository,
    internal val transaction: Transaction,
    private var refundAmount: Amount?,
    private val staxApi: StaxApi
) {

    class RefundException(message: String? = null) : StaxException("Could not refund transaction", message)

    suspend fun start(onError: (StaxException) -> Unit): Transaction? = coroutineScope {
        try {
            // Validate the refund
            validateRefund(transaction, refundAmount)?.let { throw it }

            // If no refundAmount given, assume that we want to refund the entire transaction amount
            if (refundAmount == null) {
                transaction.total?.let {
                    it.toDoubleOrNull()?.let { amountDouble ->
                        refundAmount = Amount(dollars = amountDouble)
                    }
                }
            }

            // Do the 3rd-party refund
            if (transaction.source?.contains("terminalservice.dejavoo") == true) {
                throw RefundException()
            }

            // Get the driver
            mobileReaderDriverRepository.getDriverFor(transaction).let { driver ->
                // Check if Stax refund is supported by driver
                if (driver?.isStaxRefundsSupported()!!) {
                    transaction.id?.let { transactionId ->
                        val response = staxApi.postVoidOrRefund(transactionId, refundAmount?.dollarsString()) {
                            throw RefundException()
                        }

                        if (response == null || response.success == false) {
                            throw RefundException()
                        }

                        return@coroutineScope response
                    }
                } else {
                    // Do the 3rd-party refund
                    val result = mobileReaderDriverRepository
                        .getDriverFor(transaction)
                        ?.refundTransaction(transaction, refundAmount)

                    if (result == null || result.success == false) {
                        throw RefundException()
                    }

                    postRefundedTransaction(result) {
                        throw RefundException(it.message)
                    }
                }
            }
        } catch (e: RefundException) {
            onError(e)
            return@coroutineScope null
        } catch (e: Exception) {
            onError(RefundException(e.message))
            return@coroutineScope null
        }
    }

    private suspend fun postRefundedTransaction(result: TransactionResult, error: (StaxException) -> Unit): Transaction? {
        val refundedTransaction = Transaction().apply {
            total = result.amount?.dollarsString()
            paymentMethodId = transaction.paymentMethodId
            success = result.success
            lastFour = transaction.lastFour
            type = "refund"
            source = transaction.source
            referenceId = transaction.id
            method = transaction.method
            customerId = transaction.customerId
            invoiceId = transaction.invoiceId
        }

        return transactionRepository.create(refundedTransaction, error)
    }

    companion object {
        internal fun validateRefund(transaction: Transaction, refundAmount: Amount? = null): StaxException? {
            // Ensure transaction isn't voided
            if (transaction.isVoided == true) {
                return RefundException("Can not refund voided transaction")
            }

            // Account for previous refunds
            val totalRefunded = transaction.totalRefunded?.toDoubleOrNull() ?: return null
            val transactionTotal = transaction.total?.toDoubleOrNull() ?: return null

            // Can't refund transaction that has already been refunded
            if (transactionTotal - totalRefunded < 0.01) {
                return RefundException("Can not refund transaction that has been fully refunded")
            }

            val amount = refundAmount ?: return null

            // Can't refund more than there is left to refund
            if (amount.dollars() > (transactionTotal - totalRefunded)) {
                return RefundException("Can not refund more than the original transaction total")
            }

            // Can't refund zero amount
            if (amount.dollars() <= 0.0) {
                return RefundException("Can not refund zero or negative amounts")
            }

            return null
        }
    }
}
