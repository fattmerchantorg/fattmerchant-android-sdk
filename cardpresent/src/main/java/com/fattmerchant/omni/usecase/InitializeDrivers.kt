package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Initializes all the drivers provided by the mobileReaderDriverRepository
 */
internal class InitializeDrivers(
        private val mobileReaderDriverRepository: MobileReaderDriverRepository,
        private val args: Map<String, Any>,
        override val coroutineContext: CoroutineContext
) : CoroutineScope {

    class InitializeDriversException(message: String? = null) : OmniException("Could not initialize drivers", message)

    suspend fun start(onError: (OmniException) -> Unit) {
        try {
            mobileReaderDriverRepository
                    .getDrivers()
                    .firstOrNull()?.initialize(args)
        } catch (e: Error) {
            onError(InitializeDriversException(e.message))
        }

    }
}