package dev.datlag.burningseries.shared.common

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun <T> MutableStateFlow<T>.safeEmit(value: T, scope: CoroutineScope) {
    if (!this.tryEmit(value)) {
        scope.launch(ioDispatcher()) {
            this@safeEmit.emit(value)
        }
    }
}

fun CoroutineScope(context: CoroutineContext, lifecycle: Lifecycle): CoroutineScope {
    val scope = CoroutineScope(context)
    lifecycle.doOnDestroy(scope::cancel)
    return scope
}

fun LifecycleOwner.coroutineScope(context: CoroutineContext): CoroutineScope = CoroutineScope(context, lifecycle)

fun LifecycleOwner.ioScope() = CoroutineScope(ioDispatcher() + SupervisorJob(), lifecycle)
fun LifecycleOwner.mainScope() = CoroutineScope(mainDispatcher() + SupervisorJob(), lifecycle)
fun LifecycleOwner.defaultScope() = CoroutineScope(defaultDispatcher() + SupervisorJob(), lifecycle)

fun mainDispatcher(): MainCoroutineDispatcher = Dispatchers.DeviceMain
fun ioDispatcher(): CoroutineDispatcher = Dispatchers.DeviceIO
fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.DeviceDefault

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(ioDispatcher()) {
        block()
    }
}

fun CoroutineScope.launchMain(block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(mainDispatcher()) {
        block()
    }
}

fun CoroutineScope.launchDefault(block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(defaultDispatcher()) {
        block()
    }
}

fun LifecycleOwner.launchIO(block: suspend  CoroutineScope.() -> Unit): Job {
    return ioScope().launchIO(block)
}

fun LifecycleOwner.launchMain(block: suspend  CoroutineScope.() -> Unit): Job {
    return mainScope().launchMain(block)
}

fun LifecycleOwner.launchDefault(block: suspend  CoroutineScope.() -> Unit): Job {
    return defaultScope().launchDefault(block)
}

suspend fun <T> withIOContext(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(ioDispatcher()) {
        block()
    }
}

suspend fun <T> withMainContext(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(mainDispatcher()) {
        block()
    }
}

suspend fun <T> withDefaultContext(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(defaultDispatcher()) {
        block()
    }
}

fun <T> runBlockingIO(block: suspend CoroutineScope.() -> T): T {
    return runBlocking(ioDispatcher()) {
        block()
    }
}

fun <T> runBlockingMain(block: suspend CoroutineScope.() -> T): T {
    return runBlocking(mainDispatcher()) {
        block()
    }
}

fun <T> runBlockingDefault(block: suspend CoroutineScope.() -> T): T {
    return runBlocking(defaultDispatcher()) {
        block()
    }
}