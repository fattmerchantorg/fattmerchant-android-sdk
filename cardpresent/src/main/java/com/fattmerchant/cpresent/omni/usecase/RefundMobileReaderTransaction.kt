package com.fattmerchant.cpresent.omni.usecase

import android.util.Log
import com.fattmerchant.cpresent.omni.entity.OmniClient
import com.fattmerchant.cpresent.omni.entity.models.Transaction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.CancellationException

/**
 * Refunds the given [Transaction] and records the refund in Omni
 *
 * @property omni
 * @property transaction
 */
class RefundMobileReaderTransaction(
    val omni: OmniClient,
    val transaction: Transaction
) {

    class RefundException(message: String) : Exception(message)

    suspend fun start(onError: (Error) -> Void): Transaction? = coroutineScope {
        try {
            val result = omni.mobileReaderDriverRepository
                .getDriverFor(transaction)
                ?.refundTransaction(transaction)

            //TODO: Error
            if (result == null || result.success == false) {
                throw RefundException("Could not refund transaction")
            }

            val refunded = Transaction().apply {
                paymentMethodId = transaction.paymentMethodId
                total = transaction.total
                success = true
                lastFour = transaction.lastFour
                type = "refund"
                source = transaction.source
                referenceId = transaction.id
                method = transaction.method
                customerId = transaction.customerId
                invoiceId = transaction.invoiceId
            }

            omni.transactionRepository.create(refunded) {
                onError(it)
            }
        }

        catch (e: Exception) {
            throw RefundException("Error trying to refund transaction")
        }
    }

}