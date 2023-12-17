package dev.datlag.burningseries.model.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

fun <T> scopeCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: Throwable) {
    if (e is CancellationException) {
        throw e
    }
    Result.failure(e)
}

suspend fun <T> suspendCatching(block: suspend CoroutineScope.() -> T): Result<T> = coroutineScope {
    try {
        Result.success(
            block(this)
        )
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        }
        Result.failure(e)
    }
}

fun <T> safeCast(block: () -> T?): T? {
    return scopeCatching {
        block()
    }.getOrNull()
}

suspend fun <T> suspendSafeCast(block: () -> T?): T? {
    return suspendCatching {
        block()
    }.getOrNull()
}

inline fun <reified T> Any?.safeCast(): T? {
    return safeCast {
        if (this is T) {
            this
        } else {
            this as? T?
        }
    }
}

suspend fun <T> Flow<T>.collectSafe(collector: FlowCollector<T>) {
    suspendCatching {
        this@collectSafe.collect(collector)
    }.getOrNull() ?: collector.emit(this.first())
}

suspend fun <T> StateFlow<T>.collectSafe(collector: FlowCollector<T>) {
    suspendCatching {
        this@collectSafe.collect(collector)
    }.getOrNull() ?: collector.emit(this.value)
}