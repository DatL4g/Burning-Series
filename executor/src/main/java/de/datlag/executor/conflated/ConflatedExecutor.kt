package de.datlag.executor.conflated

import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference

@Obfuscate
class ConflatedExecutor : ConflatedExecutorInterface {
    private val activeTask: AtomicReference<Job?> = AtomicReference(null)

    override suspend fun <T> conflate(block: suspend () -> T): T {
        return coroutineScope {
            val newTask = async(start = CoroutineStart.LAZY) { block() }.also { task ->
                task.invokeOnCompletion {
                    activeTask.compareAndSet(task, null)
                }
            }

            val result: T
            while(true) {
                if (!activeTask.compareAndSet(null, newTask)) {
                    activeTask.get()?.cancelAndJoin()
                    yield()
                } else {
                    result = newTask.await()
                    break
                }
            }
            result
        }
    }
}