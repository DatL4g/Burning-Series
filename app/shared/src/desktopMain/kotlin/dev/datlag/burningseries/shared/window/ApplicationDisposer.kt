package dev.datlag.burningseries.shared.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalApplicationDisposer = staticCompositionLocalOf<ApplicationDisposer> {
    error("No state set for ApplicationDisposer")
}

interface ApplicationDisposer {

    fun exit()

    fun restart()

    companion object {
        val current: ApplicationDisposer
            @Composable
            @ReadOnlyComposable
            get() = LocalApplicationDisposer.current
    }
}