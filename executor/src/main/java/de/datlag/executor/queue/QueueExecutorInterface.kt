package de.datlag.executor.queue

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface QueueExecutorInterface {

    suspend fun <T> enqueue(block: suspend () -> T): T
}