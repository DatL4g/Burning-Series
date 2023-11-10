package dev.datlag.burningseries

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.moriatsushi.insetsx.rememberWindowInsetsController
import dev.datlag.burningseries.ui.theme.Colors
import dev.datlag.burningseries.ui.theme.CommonSchemeTheme
import dev.datlag.burningseries.ui.theme.toLegacyColors
import dev.datlag.burningseries.ui.theme.toLegacyShapes
import org.kodein.di.DI

val LocalDarkMode = compositionLocalOf<Boolean> { error("No dark mode state provided") }

@Composable
fun App(
    di: DI,
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val windowInsetsController = rememberWindowInsetsController()

    LaunchedEffect(systemDarkTheme) {
        windowInsetsController?.apply {
            setStatusBarContentColor(dark = !systemDarkTheme)
            setNavigationBarsContentColor(dark = !systemDarkTheme)
        }
    }

    CompositionLocalProvider(
        LocalDarkMode provides systemDarkTheme
    ) {
        MaterialTheme(
            colorScheme = if (systemDarkTheme) Colors.getDarkScheme() else Colors.getLightScheme()
        ) {
            androidx.compose.material.MaterialTheme(
                colors = MaterialTheme.colorScheme.toLegacyColors(systemDarkTheme),
                shapes = MaterialTheme.shapes.toLegacyShapes()
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
}

@Composable
expect fun SystemProvider(content: @Composable () -> Unit)