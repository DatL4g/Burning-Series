package dev.datlag.burningseries.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

expect fun Modifier.onClick(
    enabled: Boolean = true,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) : Modifier

@Composable
expect fun isTv(): Boolean

@Composable
expect fun <T> Flow<T>.collectAsStateSafe(initial: () -> T): State<T>

@Composable
expect fun <T> StateFlow<T>.collectAsStateSafe(): State<T>
