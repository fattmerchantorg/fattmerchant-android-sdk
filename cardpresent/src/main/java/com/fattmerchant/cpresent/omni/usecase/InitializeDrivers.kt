package com.fattmerchant.cpresent.omni.usecase

import com.fattmerchant.cpresent.omni.entity.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Initializes all the drivers provided by the mobileReaderDriverRepository
 */
class InitializeDrivers(
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    val args: Map<String, Any>,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start() {
        mobileReaderDriverRepository
            .getDrivers()
            .forEach {
                try {
                    it.initialize(args)
                } catch (e: Error) {
                    //TODO: Log/handle this
                    throw e
                }
            }
    }
}