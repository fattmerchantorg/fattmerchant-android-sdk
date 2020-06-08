package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal class DisconnectMobileReader(
        override val coroutineContext: CoroutineContext,
        var mobileReaderDriverRepository: MobileReaderDriverRepository,
        var mobileReader: MobileReader
): CoroutineScope {

    suspend fun start(): Boolean {
        return try {
            mobileReaderDriverRepository
                    .getDriverFor(mobileReader)
                    ?.disconnectReader(mobileReader)
                    ?: false
        } catch (e: OmniException) {
            false
        }
    }

}