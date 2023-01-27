package dev.datlag.burningseries

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.ui.theme.*
import org.kodein.di.DI

val LocalDarkMode = compositionLocalOf<Boolean> { error("No dark mode state provided") }
val LocalResources = compositionLocalOf<Resources> { error("No resources state provided") }
val LocalStringRes = compositionLocalOf<StringRes> { error("No StringRes state provided") }
val LocalOrientation = compositionLocalOf<Orientation> { error("No Orientation state provided") }
var BackPressedListener: (() -> Unit)? = null
var NavigationListener: ((finish: Boolean) -> Unit)? = null

@Composable
fun App(
    useDarkTheme: Boolean = isSystemInDarkTheme() || getSystemDarkMode(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDarkMode provides useDarkTheme) {
        MaterialTheme(
            colorScheme = if (useDarkTheme) Colors.getDarkScheme() else Colors.getLightScheme(),
            typography = ManropeTypography()
        ) {
            androidx.compose.material.MaterialTheme(
                colors = MaterialTheme.colorScheme.toLegacyColors(useDarkTheme),
                shapes = MaterialTheme.shapes.toLegacyShapes(),
                typography = ManropeTypographyLegacy()
            ) {
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

@Composable
expect fun getSystemDarkMode(): Boolean
