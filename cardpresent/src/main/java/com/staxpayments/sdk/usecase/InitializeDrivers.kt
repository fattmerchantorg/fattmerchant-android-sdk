package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
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

    class InitializeDriversException(message: String? = null) : StaxException("Could not initialize drivers", message) {
        companion object {
            val NoMobileReadersFound = InitializeDriversException("Couldn't find any mobile readers")
        }
    }

    suspend fun start(onError: (StaxException) -> Unit) {
        // Get all the drivers
        val drivers = mobileReaderDriverRepository.getDrivers()

        var error: StaxException? = null

        // One by one, try to initialize them
        val initializedDrivers = drivers.map {
            try {
                it.initialize(args)
            } catch (e: Throwable) {
                if (e is StaxException) {
                    error = e
                }
                false
            }
        }.toMutableList()

        when {
            initializedDrivers.contains(true) -> return
            error != null -> onError(error!!)
            else -> onError(InitializeDriversException.NoMobileReadersFound)
        }
    }
}
