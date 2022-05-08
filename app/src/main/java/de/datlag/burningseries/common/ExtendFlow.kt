@file:Obfuscate

package de.datlag.burningseries.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

inline fun <T> Flow<T>.launchAndCollectIn(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minActiveState) {
        collect {
            action(it)
        }
    }
}

fun <T> MutableSharedFlow<T>.forceEmit(value: T, coroutineScope: CoroutineScope) {
    val emitted = this.tryEmit(value)
    if (!emitted) {
        coroutineScope.launch(Dispatchers.Default) {
            this@forceEmit.emit(value)
            if (this@forceEmit is MutableStateFlow) {
                withContext(Dispatchers.Main) {
                    this@forceEmit.value = value
                }
            }
        }
    }
}

fun <T> Flow<T>.toMutableSharedFlow(scope: CoroutineScope): MutableSharedFlow<T> {
    val mutableShared = MutableSharedFlow<T>()
    scope.launch(Dispatchers.IO) {
        mutableShared.emitAll(this@toMutableSharedFlow)
    }
    return mutableShared
}