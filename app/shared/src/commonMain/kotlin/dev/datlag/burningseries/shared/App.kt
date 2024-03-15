package dev.datlag.burningseries.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.shared.ui.theme.*
import org.kodein.di.DI

val LocalDarkMode = compositionLocalOf<Boolean> { error("No dark mode state provided") }
val LocalDI = compositionLocalOf<DI> { error("No dependency injection provided") }
val LocalHaze = compositionLocalOf<HazeState> { error("No Haze state provided") }
val LocalPaddingValues = compositionLocalOf<PaddingValues?> { null }

@Composable
fun App(
    di: DI,
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalDarkMode provides systemDarkTheme,
        LocalDI provides di
    ) {
        MaterialTheme(
            colorScheme = if (systemDarkTheme) Colors.getDarkScheme() else Colors.getLightScheme(),
            typography = ManropeTypography()
        ) {
            SystemProvider {
                CommonSchemeTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
expect fun SystemProvider(content: @Composable () -> Unit)

@Composable
expect fun rememberIsTv(): Boolean