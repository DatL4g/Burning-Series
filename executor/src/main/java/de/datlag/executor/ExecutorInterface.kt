package de.datlag.executor

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface ExecutorInterface {

    suspend fun <T> execute(
        schema: Schema,
        block: suspend () -> T
    ): T
}