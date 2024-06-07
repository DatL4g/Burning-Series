package dev.datlag.burningseries.other

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.datlag.tooling.Platform

@Composable
actual fun Platform.rememberIsTv(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        isTelevision(context)
    }
}