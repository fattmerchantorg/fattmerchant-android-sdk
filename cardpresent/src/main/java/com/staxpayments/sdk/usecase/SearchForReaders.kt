package com.staxpayments.sdk.usecase

import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.api.repository.MobileReaderDriverRepository
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
