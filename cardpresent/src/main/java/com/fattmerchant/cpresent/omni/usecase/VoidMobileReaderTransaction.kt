package com.fattmerchant.cpresent.omni.usecase

import com.fattmerchant.cpresent.omni.entity.models.Transaction
import com.fattmerchant.cpresent.omni.entity.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext


/**
 * Voids the given [Transaction] and records the void in Omni
 *
 * @property mobileReaderDriverRepository
 * @property transactionRepository
 * @property transaction
 * @property coroutineContext
 */
class VoidMobileReaderTransaction(
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    val transactionRepository: TransactionRepository,
    val transaction: Transaction,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(error: (Error) -> Unit): Transaction? = coroutineScope {
        mobileReaderDriverRepository
            .getDriverFor(transaction)
            ?.voidTransaction(transaction)
            ?: cancel()

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
    }

}