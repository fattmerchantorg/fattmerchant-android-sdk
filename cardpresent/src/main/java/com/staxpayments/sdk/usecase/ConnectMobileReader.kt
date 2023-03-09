package com.staxpayments.sdk.usecase

import com.staxpayments.sdk.MobileReaderConnectionStatusListener
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.models.OmniException
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
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

    /**
     * Connects the given [mobileReader]
     *
     * @return the connected [MobileReader]
     */
    suspend fun start(): MobileReader? {
        return try {
            val driver = mobileReaderDriverRepository.getDriverFor(mobileReader)
            if (driver != null) {
                driver.mobileReaderConnectionStatusListener = mobileReaderConnectionStatusListener
                return driver.connectReader(mobileReader)
            } else {
                // We couldn't find the driver, so lets ask all initialized drivers
                // to connect the reader. If all of them fail, return false
                mobileReaderDriverRepository.getInitializedDrivers().forEach {
                    it.mobileReaderConnectionStatusListener = mobileReaderConnectionStatusListener
                    it.connectReader(mobileReader)?.let { connectedReader ->
                        return connectedReader
                    }
                }
                return null
            }
        } catch (e: OmniException) {
            null
        }
    }
}
