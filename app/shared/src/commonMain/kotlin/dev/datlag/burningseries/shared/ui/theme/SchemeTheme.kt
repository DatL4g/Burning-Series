package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import dev.datlag.burningseries.color.theme.Theme
import dev.datlag.burningseries.shared.LocalDarkMode
import dev.datlag.burningseries.shared.common.animate
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.withIOContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

data object SchemeTheme {

    internal val commonSchemeKey : MutableStateFlow<Any?> = MutableStateFlow(null)
    internal val itemScheme: MutableStateFlow<Map<Any, ThemeHolder?>> = MutableStateFlow(emptyMap())

    @Composable
    fun setCommon(key: Any?) {
        LaunchedEffect(key) {
            withIOContext {
                setCommon(key, this)
            }
        }
    }

    fun setCommon(key: Any?, scope: CoroutineScope?) {
        commonSchemeKey.value = key
        scope?.launchIO {
            commonSchemeKey.emit(key)
        }
    }

    fun containsScheme(key: Any): Boolean {
        return itemScheme.value[key] != null
    }

    @Composable
    fun createColorScheme(key: Any, block: suspend CoroutineScope.() -> Theme?) {
        LaunchedEffect(key) {
            withIOContext {
                createColorScheme(key, block(), this)
            }
        }
    }

    @Composable
    fun createColorScheme(key: Any, theme: Theme?) {
        createColorScheme(key, theme, rememberCoroutineScope())
    }

    fun createColorScheme(key: Any, theme: Theme?, scope: CoroutineScope) {
        if (theme == null) return

        val newTheme = ThemeHolder(
            dark = darkColorScheme(
                primary = Color(theme.schemes.dark.primary),
                onPrimary = Color(theme.schemes.dark.onPrimary),
                primaryContainer = Color(theme.schemes.dark.primaryContainer),
                onPrimaryContainer = Color(theme.schemes.dark.onPrimaryContainer),

                secondary = Color(theme.schemes.dark.secondary),
                onSecondary = Color(theme.schemes.dark.onSecondary),
                secondaryContainer = Color(theme.schemes.dark.secondaryContainer),
                onSecondaryContainer = Color(theme.schemes.dark.onSecondaryContainer),

                tertiary = Color(theme.schemes.dark.tertiary),
                onTertiary = Color(theme.schemes.dark.onTertiary),
                tertiaryContainer = Color(theme.schemes.dark.tertiaryContainer),
                onTertiaryContainer = Color(theme.schemes.dark.onTertiaryContainer),

                error = Color(theme.schemes.dark.error),
                onError = Color(theme.schemes.dark.onError),
                errorContainer = Color(theme.schemes.dark.errorContainer),
                onErrorContainer = Color(theme.schemes.dark.onErrorContainer),

                background = Color(theme.schemes.dark.background),
                onBackground = Color(theme.schemes.dark.onBackground),

                surface = Color(theme.schemes.dark.surface),
                onSurface = Color(theme.schemes.dark.onSurface),
                surfaceVariant = Color(theme.schemes.dark.surfaceVariant),
                onSurfaceVariant = Color(theme.schemes.dark.onSurfaceVariant),

                outline = Color(theme.schemes.dark.outline),
                inverseSurface = Color(theme.schemes.dark.inverseSurface),
                inverseOnSurface = Color(theme.schemes.dark.inverseOnSurface),
                inversePrimary = Color(theme.schemes.dark.inversePrimary)
            ),
            light = lightColorScheme(
                primary = Color(theme.schemes.light.primary),
                onPrimary = Color(theme.schemes.light.onPrimary),
                primaryContainer = Color(theme.schemes.light.primaryContainer),
                onPrimaryContainer = Color(theme.schemes.light.onPrimaryContainer),

                secondary = Color(theme.schemes.light.secondary),
                onSecondary = Color(theme.schemes.light.onSecondary),
                secondaryContainer = Color(theme.schemes.light.secondaryContainer),
                onSecondaryContainer = Color(theme.schemes.light.onSecondaryContainer),

                tertiary = Color(theme.schemes.light.tertiary),
                onTertiary = Color(theme.schemes.light.onTertiary),
                tertiaryContainer = Color(theme.schemes.light.tertiaryContainer),
                onTertiaryContainer = Color(theme.schemes.light.onTertiaryContainer),

                error = Color(theme.schemes.light.error),
                onError = Color(theme.schemes.light.onError),
                errorContainer = Color(theme.schemes.light.errorContainer),
                onErrorContainer = Color(theme.schemes.light.onErrorContainer),

                background = Color(theme.schemes.light.background),
                onBackground = Color(theme.schemes.light.onBackground),

                surface = Color(theme.schemes.light.surface),
                onSurface = Color(theme.schemes.light.onSurface),
                surfaceVariant = Color(theme.schemes.light.surfaceVariant),
                onSurfaceVariant = Color(theme.schemes.light.onSurfaceVariant),

                outline = Color(theme.schemes.light.outline),
                inverseSurface = Color(theme.schemes.light.inverseSurface),
                inverseOnSurface = Color(theme.schemes.light.inverseOnSurface),
                inversePrimary = Color(theme.schemes.light.inversePrimary),
            )
        )

        scope.launchIO {
            val currentMap = (itemScheme.firstOrNull() ?: itemScheme.value).toMutableMap()
            currentMap[key] = newTheme
            itemScheme.emit(currentMap)
        }
    }

    data object COLOR_KEY {
        private const val SUFFIX = "ColorKey"

        const val ERROR = "Error$SUFFIX"
    }
}

@Composable
fun SchemeTheme(key: Any?, content: @Composable () -> Unit) {
    val themeHolder by SchemeTheme.itemScheme.map {
        it.firstNotNullOfOrNull { entry ->
            if (entry.key == key) {
                entry.value
            } else {
                null
            }
        }
    }.collectAsStateWithLifecycle(initialValue = null)

    val scheme = (if (LocalDarkMode.current) themeHolder?.dark else themeHolder?.light) ?: MaterialTheme.colorScheme

    MaterialTheme(
        colorScheme = scheme.animate()
    ) {
        androidx.compose.material.MaterialTheme(
            colors = MaterialTheme.colorScheme.toLegacyColors(LocalDarkMode.current)
        ) {
            SchemeThemeSystemProvider(scheme) {
                content()
            }
        }
    }
}

@Composable
fun CommonSchemeTheme(content: @Composable () -> Unit) {
    val key by SchemeTheme.commonSchemeKey.collectAsStateWithLifecycle()

    SchemeTheme(key, content)
}

data class ThemeHolder(
    val dark: ColorScheme,
    val light: ColorScheme
)

@Composable
expect fun SchemeThemeSystemProvider(scheme: ColorScheme, content: @Composable () -> Unit)

@Composable
expect fun loadImageScheme(key: Any, painter: Painter)