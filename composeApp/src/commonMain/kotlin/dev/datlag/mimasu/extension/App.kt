package dev.datlag.mimasu.extension

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*

val LocalDarkMode = compositionLocalOf<Boolean> { error("No dark mode state provided") }
val LocalEdgeToEdge = staticCompositionLocalOf<Boolean> { false }

@Composable
internal fun App(
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalDarkMode provides systemDarkTheme
    ) {
        Column {
            Text("Hello World from Mimasu Extension")
            content()
        }
    }
}