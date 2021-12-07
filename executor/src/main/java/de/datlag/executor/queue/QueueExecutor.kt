package de.datlag.executor.queue

import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Obfuscate
class QueueExecutor : QueueExecutorInterface {
    private val mutex: Mutex = Mutex()

    override suspend fun <T> enqueue(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }
}