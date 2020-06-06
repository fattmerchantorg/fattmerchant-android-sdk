package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import com.fattmerchant.omni.data.repository.TransactionRepository
import kotlinx.coroutines.coroutineScope

/**
 * Refunds the given [Transaction] and records the refund in Omni
 *
 * @property mobileReaderDriverRepository
 * @property transactionRepository
 * @property transaction
 */
internal class RefundMobileReaderTransaction(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val transactionRepository: TransactionRepository,
    internal val transaction: Transaction,
    internal val refundAmount: Amount?
) {

    class RefundException(message: String? = null) : OmniException("Could not refund transaction", message)

    suspend fun start(onError: (OmniException) -> Unit): Transaction? = coroutineScope {
        try {

            // Validate the refund
            validateRefund(transaction, refundAmount)?.let { throw it }

            // Do the 3rd-party refund
            val result = mobileReaderDriverRepository
                .getDriverFor(transaction)
                ?.refundTransaction(transaction, refundAmount)

            if (result == null || result.success == false) {
                throw RefundException()
            }

            val refunded = Transaction().apply {
                paymentMethodId = transaction.paymentMethodId
                total = result.amount?.dollarsString()
                success = result.success
                lastFour = transaction.lastFour
                type = "refund"
                source = transaction.source
                referenceId = transaction.id
                method = transaction.method
                customerId = transaction.customerId
                invoiceId = transaction.invoiceId
            }

            transactionRepository.create(refunded) {
                throw RefundException(it.message)
            }

        } catch (e: RefundException) {
            onError(e)
            return@coroutineScope null
        } catch (e: Exception) {
            onError(RefundException(e.message))
            return@coroutineScope null
        }
    }

    companion object {
        internal fun validateRefund(transaction: Transaction, refundAmount: Amount? = null): OmniException? {
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