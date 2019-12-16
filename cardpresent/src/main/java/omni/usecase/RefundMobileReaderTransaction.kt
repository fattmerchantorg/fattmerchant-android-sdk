package omni.usecase

import omni.data.models.OmniException
import omni.data.models.Transaction
import omni.data.repository.MobileReaderDriverRepository
import omni.data.repository.TransactionRepository
import kotlinx.coroutines.coroutineScope

/**
 * Refunds the given [Transaction] and records the refund in Omni
 *
 * @property mobileReaderDriverRepository
 * @property transactionRepository
 * @property transaction
 */
class RefundMobileReaderTransaction(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val transactionRepository: TransactionRepository,
    private val transaction: Transaction
) {

    class RefundException(message: String? = null) : OmniException("Could not refund transaction", message)

    suspend fun start(onError: (OmniException) -> Unit): Transaction? = coroutineScope {
        try {

            // Can't refund transaction that has been voided
            if (transaction.isVoided == true) {
                throw RefundException("Can not refund voided transaction")
            }

            // Can't refund transaction that has been refunded already
            if (transaction.totalRefunded != "0") {
                throw RefundException("Transaction already refunded")
            }

            // Do the 3rd-party refund
            val result = mobileReaderDriverRepository
                .getDriverFor(transaction)
                ?.refundTransaction(transaction)

            if (result == null || result.success == false) {
                throw RefundException()
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

}