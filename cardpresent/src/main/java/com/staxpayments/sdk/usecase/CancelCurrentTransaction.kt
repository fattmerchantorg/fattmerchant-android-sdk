package com.staxpayments.sdk.usecase

import com.staxpayments.sdk.data.models.OmniException
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class CancelCurrentTransactionException(message: String? = null) : OmniException("", message) {
    companion object {
        val NoTransactionToCancel = CancelCurrentTransactionException("There is no transaction to cancel")
        val Unknown = CancelCurrentTransactionException("Unknown error")
    }
}

/**
 * Cancels a current mobile reader [transaction]
 */
internal class CancelCurrentTransaction(
    override val coroutineContext: CoroutineContext,
    var mobileReaderDriverRepository: MobileReaderDriverRepository
) : CoroutineScope {

    /**
     * Attempt to cancel a current mobile reader [transaction]
     *
     * @return result of cancelling [transaction] attempt
     */
    suspend fun start(error: (OmniException) -> Unit): Boolean {
        val drivers = mobileReaderDriverRepository.getInitializedDrivers()
        var success = true
        drivers.forEach { driver ->
            success = success && driver.cancelCurrentTransaction(error)
        }
        return success
    }
}
