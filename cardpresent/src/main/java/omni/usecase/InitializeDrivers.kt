package omni.usecase

import omni.data.models.OmniException
import omni.data.repository.MobileReaderDriverRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Initializes all the drivers provided by the mobileReaderDriverRepository
 */
class InitializeDrivers(
    private val mobileReaderDriverRepository: MobileReaderDriverRepository,
    private val args: Map<String, Any>,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    class InitializeDriversException(message: String? = null) : OmniException("Could not initialize drivers", message)

    suspend fun start() {
        mobileReaderDriverRepository
            .getDrivers()
            .forEach {
                try {
                    it.initialize(args)
                } catch (e: Error) {
                    //TODO: Log/handle this
                    throw e
                }
            }
    }
}