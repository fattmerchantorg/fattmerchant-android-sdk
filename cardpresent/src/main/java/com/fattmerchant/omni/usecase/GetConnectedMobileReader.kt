package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.OmniGeneralException
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
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
