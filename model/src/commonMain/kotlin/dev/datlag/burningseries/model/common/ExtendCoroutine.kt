package dev.datlag.burningseries.model.common

import dev.datlag.burningseries.model.state.CatchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull

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

suspend fun <T> suspendCatchResult(block: suspend CoroutineScope.() -> T): CatchResult<T & Any> = coroutineScope {
    val result = suspendCatching(block)
    return@coroutineScope if (result.isFailure) {
        CatchResult.Error(result.exceptionOrNull())
    } else {
        result.getOrNull()?.let {
            CatchResult.Success(it)
        } ?: CatchResult.Error(result.exceptionOrNull())
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
    }.getOrNull() ?: suspendCatching {
        this@collectSafe.firstOrNull()
    }.getOrNull()?.let { collector.emit(it) }
}

suspend fun <T> StateFlow<T>.collectSafe(collector: FlowCollector<T>) {
    suspendCatching {
        this@collectSafe.collect(collector)
    }.getOrNull() ?: collector.emit(this.value)
}