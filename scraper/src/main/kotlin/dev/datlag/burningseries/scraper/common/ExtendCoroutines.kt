package dev.datlag.burningseries.scraper.common

import kotlinx.coroutines.*

suspend fun <T> suspendCatching(block: suspend CoroutineScope.() -> T): Result<T> = coroutineScope {
    return@coroutineScope try {
        Result.success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        } else {
            Result.failure(e)
        }
    }
}
