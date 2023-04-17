package dev.datlag.burningseries.network.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

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