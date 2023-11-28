package dev.datlag.burningseries.shared

import androidx.compose.runtime.Composable

@Composable
actual fun SystemProvider(content: @Composable () -> Unit) {
    content()
}