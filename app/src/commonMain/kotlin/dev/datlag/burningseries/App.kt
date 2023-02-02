package dev.datlag.burningseries

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import dev.datlag.burningseries.common.collectAsStateSafe
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.datastore.common.appearance
import dev.datlag.burningseries.datastore.common.appearanceAmoled
import dev.datlag.burningseries.datastore.common.appearanceThemeMode
import dev.datlag.burningseries.datastore.preferences.AppSettings
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.ui.theme.*
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.instance
import dev.datlag.burningseries.other.Logger

val LocalDarkMode = compositionLocalOf<Boolean> { error("No dark mode state provided") }
val LocalResources = compositionLocalOf<Resources> { error("No resources state provided") }
val LocalStringRes = compositionLocalOf<StringRes> { error("No StringRes state provided") }
val LocalOrientation = compositionLocalOf<Orientation> { error("No Orientation state provided") }
var BackPressedListener: (() -> Unit)? = null
var NavigationListener: ((finish: Boolean) -> Unit)? = null

@Composable
fun App(
    di: DI,
    nightMode: Int = 0,
    systemDarkTheme: Boolean = isSystemInDarkTheme() || getSystemDarkMode(),
    content: @Composable () -> Unit
) {
    val settings: DataStore<AppSettings> by di.instance()

    val themeMode by settings.appearanceThemeMode.collectAsStateSafe { settings.appearanceThemeMode.getValueBlocking(nightMode) }
    val amoled by settings.appearanceAmoled.collectAsStateSafe { settings.appearanceAmoled.getValueBlocking(false) }
    val useDarkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDarkTheme
    }

    CompositionLocalProvider(LocalDarkMode provides useDarkTheme) {
        setSystemAppearance(themeMode, useDarkTheme, amoled)
        MaterialTheme(
            colorScheme = if (useDarkTheme) Colors.getDarkScheme(amoled) else Colors.getLightScheme(),
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

@Composable
expect fun setSystemAppearance(themeMode: Int, usesDarkMode: Boolean, usesAmoled: Boolean)
