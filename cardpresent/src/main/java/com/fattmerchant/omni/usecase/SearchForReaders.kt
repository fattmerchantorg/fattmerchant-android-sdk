package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Searches for all readers in the mobileReaderDriverRepository
 */
internal class SearchForReaders(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val args: Map<String, Any>,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(): List<MobileReader> = mobileReaderDriverRepository
        .getInitializedDrivers()
        .flatMap { it.searchForReaders(args) }

}