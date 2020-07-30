package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Connects a single mobile reader from the [mobileReaderDriverRepository]
 */
internal class CancelCurrentTransaction(
        override val coroutineContext: CoroutineContext,
        var mobileReaderDriverRepository: MobileReaderDriverRepository
) : CoroutineScope {

    class CancelCurrentTransactionException(message: String? = null) : OmniException("", message) {
        companion object {
            val NoTransactionToCancel = CancelCurrentTransactionException("There is no transaction to cancel")
            val Unknown =  CancelCurrentTransactionException("Unknown error")
        }
    }

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