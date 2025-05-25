package dev.datlag.mimasu.extension

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

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
        Column(modifier = Modifier.fillMaxSize().padding(WindowInsets.statusBars.asPaddingValues())) {
            Text("Hello World from Mimasu Extension")
            content()
        }
    }
}