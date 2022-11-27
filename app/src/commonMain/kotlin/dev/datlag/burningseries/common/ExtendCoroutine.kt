package dev.datlag.burningseries.common

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
