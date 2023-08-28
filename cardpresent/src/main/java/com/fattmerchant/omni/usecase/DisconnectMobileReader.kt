package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class DisconnectMobileReaderException(detail: String) : OmniException("Could not disconnect mobile reader", detail) {
    companion object {
        val driverNotFound = DisconnectMobileReaderException("Driver not found")
    }
}

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
    private var mobileReader: MobileReader?,
) : CoroutineScope {

    /**
     * Disconnects the mobile reader
     *
     * @param onFail a block to execute in case the disconnection fails
     * @return true if the reader was disconnected. False otherwise
     */
    suspend fun start(onFail: (OmniException) -> Unit): Boolean {
        mobileReaderDriverRepository.getDriverFor(mobileReader)?.let {
            return it.disconnect(mobileReader, onFail)
        } ?: onFail(DisconnectMobileReaderException.driverNotFound)

        return false
    }
}
