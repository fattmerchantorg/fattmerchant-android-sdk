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

    class InitializeDriversException(message: String? = null) : OmniException("Could not initialize drivers", message) {
        companion object {
            val NoMobileReadersFound = InitializeDriversException("Couldn't find any mobile readers")
        }
    }

    suspend fun start(onError: (OmniException) -> Unit) {
        // Get all the drivers
        val drivers = mobileReaderDriverRepository.getDrivers()

        var error: OmniException? = null

        // One by one, try to initialize them
        val initializedDrivers = drivers.map {
            try {
                it.initialize(args)
            } catch (e: Throwable) {
                if (e is OmniException) {
                    error = e
                }
                false
            }
        }

        when {
            initializedDrivers.contains(true) -> return
            error != null -> onError(error!!)
            else -> onError(InitializeDriversException.NoMobileReadersFound)
        }
    }
}
