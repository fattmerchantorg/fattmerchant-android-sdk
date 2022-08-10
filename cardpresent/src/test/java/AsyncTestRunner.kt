import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before

interface AsyncTestRunner {

    val mainThreadSurrogate: ExecutorCoroutineDispatcher
    val scope: CoroutineScope

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    /**
     * Runs an asynchronous [operation] and cancels it if it takes longer than [timeout]
     *
     * @param timeout - Timeout in milliseconds
     * @param operation - Block to execute
     */
    fun asyncTest(timeout: Long? = 50000, operation: (() -> Unit) -> Unit) {
        runBlocking {
            withContext(scope.coroutineContext) {
                val timeoutJob = launch {
                    delay(timeout ?: 50000)
                    assert(false) {
                        "Timeout"
                    }
                }

                scope.launch {
                    operation {
                        timeoutJob.cancel()
                    }
                }
            }
        }
    }
}
