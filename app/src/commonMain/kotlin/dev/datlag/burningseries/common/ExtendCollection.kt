package dev.datlag.burningseries.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

suspend fun <T, R> Collection<T>.mapAsync(dispatcher: CoroutineDispatcher = CommonDispatcher.IO, transform: suspend (T) -> R): List<R> =
    withContext(dispatcher) {
        return@withContext this@mapAsync.map {
            async(dispatcher) {
                transform(it)
            }
        }.awaitAll()
    }