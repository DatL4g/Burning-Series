package dev.datlag.burningseries.ui.custom

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.datlag.burningseries.network.Status
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import dev.datlag.burningseries.common.CommonDispatcher

@Composable
fun snackbarHandlerForStatus(
    state: SnackbarHostState,
    status: Flow<Status>,
    mapper: (Status) -> String?,
    onReceive: ((Status) -> Unit)?
) {
    val channel = remember { Channel<Pair<Status, String>>(Channel.Factory.CONFLATED) }

    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { (status, message) ->
            withContext(CommonDispatcher.Main) {
                onReceive?.invoke(status)
            }
            state.showSnackbar(message)
        }
    }

    LaunchedEffect(status) {
        status.collect { status ->
            val message = withContext(CommonDispatcher.Main) {
                mapper(status)
            }
            message?.let { channel.send(status to it) }
        }
    }
}

@Composable
fun snackbarHandlerForStatus(
    state: SnackbarHostState,
    status: Flow<Status>,
    mapper: (Status) -> String?
) = snackbarHandlerForStatus(
    state = state,
    status = status,
    mapper = mapper,
    onReceive = null
)

data class SnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)