package dev.datlag.burningseries.shared.common.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import dev.datlag.burningseries.model.common.collectSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow

suspend fun <T> Flow<T>.collectOnLifecycle(
    lifecycle: Lifecycle,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) {
    lifecycle.repeatOnLifecycle(state) {
        collectSafe(collector)
    }
}

suspend fun <T> Flow<T>.collectOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) {
    lifecycleOwner.repeatOnLifecycle(state) {
        collectSafe(collector)
    }
}

@Composable
fun <T> Flow<T>.collectOnLocalLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) {
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycle) {
        lifecycle.repeatOnLifecycle(state) {
            collectSafe(collector)
        }
    }
}

fun <T> Flow<T>.flowWithLifecycle(
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Flow<T> = callbackFlow {
    lifecycle.repeatOnLifecycle(minActiveState) {
        this@flowWithLifecycle.collectSafe {
            send(it)
        }
    }
    close()
}

fun <T> Flow<T>.flowWithLifecycle(
    lifecycle: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Flow<T> = callbackFlow {
    lifecycle.repeatOnLifecycle(minActiveState) {
        this@flowWithLifecycle.collectSafe {
            send(it)
        }
    }
    close()
}

@Composable
fun <T> Flow<T>.withLocalLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Flow<T> {
    val lifecycle = LocalLifecycleOwner.current
    return callbackFlow {
        lifecycle.repeatOnLifecycle(minActiveState) {
            this@withLocalLifecycle.collectSafe {
                send(it)
            }
        }
        close()
    }
}