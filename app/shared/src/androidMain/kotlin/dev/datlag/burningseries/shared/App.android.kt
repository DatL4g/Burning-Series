package dev.datlag.burningseries.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.datlag.burningseries.shared.common.isTv

@Composable
actual fun SystemProvider(content: @Composable () -> Unit) {
    content()
}

@Composable
actual fun isTv(): Boolean {
    val context = LocalContext.current
    return remember { context.isTv() }
}