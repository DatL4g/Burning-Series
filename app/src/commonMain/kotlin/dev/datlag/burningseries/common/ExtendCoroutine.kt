package dev.datlag.burningseries.common

import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext


expect object CommonDispatcher {
    val IO: CoroutineDispatcher
    val Main: CoroutineDispatcher
}

fun CoroutineScope(context: CoroutineContext, lifecycle: Lifecycle): CoroutineScope {
    val scope = CoroutineScope(context)
    lifecycle.doOnDestroy(scope::cancel)
    return scope
}

fun LifecycleOwner.coroutineScope(context: CoroutineContext): CoroutineScope = CoroutineScope(context, lifecycle)

fun <T> Flow<T>.getValueBlocking(fallback: T): T {
    return runCatching {
        runBlocking(Dispatchers.IO) {
            this@getValueBlocking.first()
        }
    }.getOrNull() ?: runCatching {
        runBlocking {
            this@getValueBlocking.first()
        }
    }.getOrNull() ?: fallback
}

fun CoroutineScope.dispatch(dispatcher: CoroutineDispatcher): CoroutineScope {
    return CoroutineScope(this.coroutineContext + dispatcher)
}

fun <T : Any> Value<T>.toFlow(): Flow<T> = channelFlow {
    val scope = CoroutineScope(currentCoroutineContext())
    val observer: (T) -> Unit = {
        if (trySend(it).isFailure) {
            scope.launch(CommonDispatcher.IO) {
                if (trySend(it).isFailure) {
                    if (trySendBlocking(it).isFailure) {
                        withContext(CommonDispatcher.Main) {
                            trySendBlocking(it)
                        }
                    }
                }
            }
        }
    }

    withContext(CommonDispatcher.Main) {
        this@toFlow.subscribe(observer)
    }

    awaitClose {
        this@toFlow.unsubscribe(observer)
    }
}

fun <T> MutableStateFlow<T>.safeEmit(value: T, scope: CoroutineScope) {
    if (!this.tryEmit(value)) {
        scope.launch(Dispatchers.IO) {
            this@safeEmit.emit(value)
        }
    }
}
