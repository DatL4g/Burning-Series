package dev.datlag.burningseries.model.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface QueueExecutor {
    suspend fun <T> enqueue(block: suspend () -> T): T
}

internal class QueueExecutorImpl : QueueExecutor {
    private val mutex = Mutex()

    override suspend fun <T> enqueue(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }
}