package de.datlag.executor.conflated

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface ConflatedExecutorInterface {

    suspend fun <T> conflate(block: suspend () -> T): T
}