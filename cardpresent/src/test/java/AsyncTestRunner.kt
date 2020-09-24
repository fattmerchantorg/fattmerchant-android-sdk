import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

interface AsyncTestRunner {

    val mainThreadSurrogate: ExecutorCoroutineDispatcher
    val scope: CoroutineScope

    @BeforeAll
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterAll
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