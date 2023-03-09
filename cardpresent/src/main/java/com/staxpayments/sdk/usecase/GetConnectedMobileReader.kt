package com.staxpayments.sdk.usecase

import com.staxpayments.sdk.OmniGeneralException
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.MobileReaderDriver
import com.staxpayments.sdk.data.models.OmniException
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class GetConnectedMobileReaderException(detail: String) : OmniException("Could not get connected mobile reader", detail) {
    companion object {
        val noReaderAvailable = GetConnectedMobileReaderException("No mobile reader is available")
    }
}

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
    suspend fun start(onError: (OmniException) -> Unit): MobileReader? {
        try {
            val driver = mobileReaderDriverRepository.getInitializedDrivers().firstOrNull()

            if (driver == null) {
                onError(GetConnectedMobileReaderException.noReaderAvailable)
                return null
            }

            return driver.getConnectedReader()
        } catch (e: OmniException) {
            onError(e)
        } catch (e: Error) {
            onError(OmniGeneralException.unknown)
        }

        return null
    }
}
