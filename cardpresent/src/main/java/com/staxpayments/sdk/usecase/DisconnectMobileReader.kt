package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.DisconnectMobileReaderException
import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Disconnects the mobile reader
 *
 * @property coroutineContext
 * @property mobileReaderDriverRepository
 * @property mobileReader the [MobileReader] to disconnect. If none is passed, the currently-connected
 * one will be disconnected
 */
internal class DisconnectMobileReader(
    override val coroutineContext: CoroutineContext,
    private var mobileReaderDriverRepository: MobileReaderDriverRepository,
    private var mobileReader: MobileReader
) : CoroutineScope {

    /**
     * Disconnects the mobile reader
     *
     * @param onFail a block to execute in case the disconnection fails
     * @return true if the reader was disconnected. False otherwise
     */
    suspend fun start(onFail: (StaxException) -> Unit): Boolean {
        mobileReaderDriverRepository.getDriverFor(mobileReader)?.let {
            return it.disconnect(mobileReader, onFail)
        } ?: onFail(DisconnectMobileReaderException.driverNotFound)

        return false
    }
}
