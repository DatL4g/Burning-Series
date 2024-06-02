package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable

@Composable
actual fun AndroidFixWindowSize(content: @Composable () -> Unit) {
    content()
}