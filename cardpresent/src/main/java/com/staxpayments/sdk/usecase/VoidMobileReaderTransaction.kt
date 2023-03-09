package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.StaxException
import com.staxpayments.api.models.Transaction
import com.staxpayments.api.repository.MobileReaderDriverRepository
import com.staxpayments.api.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Voids the given [Transaction] and records the void in Stax
 *
 * @property mobileReaderDriverRepository
 * @property transactionRepository
 * @property transaction
 * @property coroutineContext
 */
internal class VoidMobileReaderTransaction(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val transactionRepository: TransactionRepository,
    val transaction: Transaction,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    class VoidTransactionException(message: String? = null) : StaxException("Error voiding transaction", message)

    suspend fun start(error: (StaxException) -> Unit): Transaction? = coroutineScope {

        try {

            // Can't void a voided transaction
            if (transaction.isVoided == true) {
                throw VoidTransactionException("Transaction already voided")
            }

            // Can't void a refunded transaction
            transaction.totalRefunded?.toFloatOrNull()?.let {
                if (it > 0) {
                    throw VoidTransactionException("Can not void refunded transaction")
                }
            }

            // Do the 3rd-party refund
            val result = if (transaction.source?.contains("terminalservice.dejavoo") == true) {
                null
            } else {
                mobileReaderDriverRepository
                    .getDriverFor(transaction)
                    ?.voidTransaction(transaction)
            }

            if (result == null || result.success == false) {
                throw VoidTransactionException()
            }

            // Make a new transaction representing the void
            val voided = Transaction().apply {
                paymentMethodId = transaction.paymentMethodId
                total = transaction.total
                success = true
                lastFour = transaction.lastFour
                type = "void"
                source = transaction.source
                referenceId = transaction.id
                method = transaction.method
                customerId = transaction.customerId
                invoiceId = transaction.invoiceId
            }

            transactionRepository.create(voided) {
                error(it)
            }
        } catch (e: VoidTransactionException) {
            error(e)
            return@coroutineScope null
        } catch (e: Exception) {
            error(VoidTransactionException())
            return@coroutineScope null
        }
    }
}
