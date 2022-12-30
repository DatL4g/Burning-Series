package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable

@Composable
actual fun DialogSurface(content: @Composable () -> Unit) {
    content()
}