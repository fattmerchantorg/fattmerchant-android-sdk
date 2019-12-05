package com.fattmerchant.cpresent.omni.usecase

import com.fattmerchant.cpresent.omni.entity.MobileReader
import com.fattmerchant.cpresent.omni.entity.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Searches for all readers in the mobileReaderDriverRepository
 */
class SearchForReaders(
    val mobileReaderDriverRepository: MobileReaderDriverRepository,
    val args: Map<String, Any>,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(): List<MobileReader> = mobileReaderDriverRepository
        .getDrivers()
        .flatMap { it.searchForReaders(args) }

}