package dev.datlag.burningseries.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.onClick(
    enabled: Boolean,
    onDoubleClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onClick: () -> Unit
): Modifier {
    return this.onClick(
        enabled = enabled,
        onDoubleClick = onDoubleClick,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

@Composable
actual fun isTv(): Boolean {
    return false
}

@Composable
actual fun <T> Flow<T>.collectAsStateSafe(initial: () -> T): State<T> = this.collectAsState(initial())

@Composable
actual fun <T> StateFlow<T>.collectAsStateSafe(): State<T> = this.collectAsState(this.value)