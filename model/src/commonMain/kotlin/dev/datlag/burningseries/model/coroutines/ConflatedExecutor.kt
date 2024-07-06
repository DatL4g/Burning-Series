package dev.datlag.burningseries.model.coroutines

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*

interface ConflatedExecutor {
    suspend fun <T> conflate(block: suspend () -> T): T
}

internal class ConflatedExecutorImpl : ConflatedExecutor {
    private val activeTask: AtomicRef<Job?> = atomic(null)

    suspend override fun <T> conflate(block: suspend () -> T): T {
        return coroutineScope {
            val newTask = async(start = CoroutineStart.LAZY) { block() }.also { task ->
                task.invokeOnCompletion {
                    activeTask.compareAndSet(task, null)
                }
            }

            val result: T
            while (true) {
                if (!activeTask.compareAndSet(null, newTask)) {
                    activeTask.value?.cancelAndJoin()
                    yield()
                } else {
                    result = newTask.await()
                    break
                }
            }
            return@coroutineScope result
        }
    }
}