package omni.usecase

import omni.data.MobileReader
import omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Searches for all readers in the mobileReaderDriverRepository
 */
class SearchForReaders(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val args: Map<String, Any>,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(): List<MobileReader> = mobileReaderDriverRepository
        .getDrivers()
        .flatMap { it.searchForReaders(args) }

}