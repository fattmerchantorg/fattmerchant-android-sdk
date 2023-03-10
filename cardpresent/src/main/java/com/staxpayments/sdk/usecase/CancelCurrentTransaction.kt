package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.StaxException
import com.staxpayments.api.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

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
    suspend fun start(error: (StaxException) -> Unit): Boolean {
        val drivers = mobileReaderDriverRepository.getInitializedDrivers()
        var success = true
        drivers.forEach { driver ->
            success = success && driver.cancelCurrentTransaction(error)
        }
        return success
    }
}
