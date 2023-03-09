package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.GetConnectedMobileReaderException
import com.staxpayments.exceptions.StaxException
import com.staxpayments.exceptions.StaxGeneralException
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.MobileReaderDriver
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Finds the connected mobile reader
 *
 * If no mobile reader is connected, returns null
 *
 * @property mobileReaderDriverRepository provides the [MobileReaderDriver]
 */
internal class GetConnectedMobileReader(
    override val coroutineContext: CoroutineContext,
    private val mobileReaderDriverRepository: MobileReaderDriverRepository
) : CoroutineScope {

    /**
     * Starts the job to connect the mobile reader
     *
     * @return the connected [MobileReader], or if none found
     */
    suspend fun start(onError: (StaxException) -> Unit): MobileReader? {
        try {
            val driver = mobileReaderDriverRepository.getInitializedDrivers().firstOrNull()

            if (driver == null) {
                onError(GetConnectedMobileReaderException.noReaderAvailable)
                return null
            }

            return driver.getConnectedReader()
        } catch (e: StaxException) {
            onError(e)
        } catch (e: Error) {
            onError(StaxGeneralException.unknown)
        }

        return null
    }
}
