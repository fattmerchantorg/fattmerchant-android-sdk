package com.fattmerchant.cpresent.omni.usecase

import com.fattmerchant.cpresent.omni.entity.MobileReader
import com.fattmerchant.cpresent.omni.entity.models.OmniException
import com.fattmerchant.cpresent.omni.entity.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Connects a single mobile reader from the [mobileReaderDriverRepository]
 */
class ConnectMobileReader(
    override val coroutineContext: CoroutineContext,
    var mobileReaderDriverRepository: MobileReaderDriverRepository,
    var mobileReader: MobileReader
) : CoroutineScope {

    suspend fun start(): Boolean {
        try {
            mobileReaderDriverRepository
                .getDrivers()
                .forEach {
                    if (it.connectReader(mobileReader)) {
                        return true
                    }
                }

            return false
        }

        catch (e: OmniException) {
            throw e
        }
    }
}