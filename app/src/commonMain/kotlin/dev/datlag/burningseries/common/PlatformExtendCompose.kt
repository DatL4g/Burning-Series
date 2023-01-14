package dev.datlag.burningseries.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect fun Modifier.onClick(
    enabled: Boolean = true,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) : Modifier

@Composable
expect fun isTv(): Boolean