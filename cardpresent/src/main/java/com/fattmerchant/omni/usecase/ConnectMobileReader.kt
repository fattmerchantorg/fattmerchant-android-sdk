package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Connects a single mobile reader from the [mobileReaderDriverRepository]
 */
internal class ConnectMobileReader(
    override val coroutineContext: CoroutineContext,
    var mobileReaderDriverRepository: MobileReaderDriverRepository,
    var mobileReader: MobileReader,
    var mobileReaderConnectionStatusListener: MobileReaderConnectionStatusListener? = null
) : CoroutineScope {

    suspend fun start(): Boolean {
        return try {
            return mobileReaderDriverRepository.getDriverFor(mobileReader)?.let { driver ->
                driver.mobileReaderConnectionStatusListener = mobileReaderConnectionStatusListener
                driver.connectReader(mobileReader)
            } ?: false
        } catch (e: OmniException) {
            false
        }
    }
}