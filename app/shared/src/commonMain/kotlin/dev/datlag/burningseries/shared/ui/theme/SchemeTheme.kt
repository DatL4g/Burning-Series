package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import com.kmpalette.DominantColorState
import com.kmpalette.palette.graphics.Palette
import com.kmpalette.rememberPainterDominantColorState
import com.materialkolor.DynamicMaterialTheme
import dev.datlag.burningseries.shared.LocalDarkMode
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.withIOContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

data object SchemeTheme {

    internal val commonSchemeKey = MutableStateFlow<Any?>(null)
    internal val colorState = MutableStateFlow<Map<Any, DominantColorState<Painter>>>(emptyMap())
    internal val itemScheme = MutableStateFlow<Map<Any, Color?>>(emptyMap())

    internal var _state: DominantColorState<Painter>? = null
    internal val state: DominantColorState<Painter>
        get() = _state!!

    fun setCommon(key: Any?) {
        commonSchemeKey.update { key }
    }

    @Composable
    fun update(key: Any?, input: Painter?) {
        if (_state == null || input == null) {
            return
        }

        LaunchedEffect(key, input) {
            suspendUpdate(key, input)
        }
    }

    fun update(key: Any?, input: Painter?, scope: CoroutineScope) {
        scope.launchIO {
            suspendUpdate(key, input)
        }
    }

    suspend fun suspendUpdate(key: Any?, input: Painter?) {
        if (_state == null || key == null ||  input == null) {
            return
        }

        withIOContext {
            val useState = (colorState.firstOrNull() ?: colorState.value)[key] ?: state
            useState.updateFrom(input)

            itemScheme.getAndUpdate {
                it.toMutableMap().apply {
                    put(key, useState.color)
                }
            }
        }
    }
}

@Composable
fun rememberSchemeThemeDominantColor(
    key: Any?
): Color? {
    if (SchemeTheme._state == null) {
        SchemeTheme._state = rememberPainterDominantColorState(
            coroutineContext = ioDispatcher()
        )
    }

    val color by remember(key) {
        SchemeTheme.itemScheme.map { it[key] }
    }.collectAsStateWithLifecycle(SchemeTheme.itemScheme.value[key])

    return color
}

@Composable
fun rememberSchemeThemeDominantColorState(
    key: Any?,
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    defaultOnColor: Color = MaterialTheme.colorScheme.onPrimary,
    coroutineContext: CoroutineContext = ioDispatcher(),
    isSwatchValid: (Palette.Swatch) -> Boolean = { true },
    builder: Palette.Builder.() -> Unit = {},
): DominantColorState<Painter> {
    val state by remember(key) {
        SchemeTheme.colorState.map { it[key] }
    }.collectAsStateWithLifecycle(SchemeTheme.colorState.value[key])

    return state ?: rememberPainterDominantColorState(
        defaultColor = defaultColor,
        defaultOnColor = defaultOnColor,
        coroutineContext = coroutineContext,
        builder = builder,
        isSwatchValid = isSwatchValid
    ).also {
        if (key != null) {
            SchemeTheme.colorState.update { map ->
                map.toMutableMap().apply {
                    put(key, it)
                }
            }
        }
    }
}

@Composable
fun rememberSchemeThemeDominantColorState(
    key: Any?,
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    defaultOnColor: Color = MaterialTheme.colorScheme.onPrimary,
    clearFilter: Boolean = false,
    applyMinContrast: Boolean = false,
    minContrastBackgroundColor: Color = Color.Transparent,
    coroutineContext: CoroutineContext = ioDispatcher()
): DominantColorState<Painter> {
    return rememberSchemeThemeDominantColorState(
        key = key,
        defaultColor = defaultColor,
        defaultOnColor = defaultOnColor,
        coroutineContext = coroutineContext,
        builder = {
            if (clearFilter) {
                clearFilters()
            } else {
                addFilter(Palette.DEFAULT_FILTER)
            }
        },
        isSwatchValid = { swatch ->
            if (applyMinContrast) {
                Color(swatch.bodyTextColor).contrastAgainst(minContrastBackgroundColor) >= MinContrastRatio
            } else {
                true
            }
        }
    )
}

@Composable
fun SchemeTheme(key: Any?, content: @Composable () -> Unit) {
    DynamicMaterialTheme(
        seedColor = rememberSchemeThemeDominantColor(key) ?: MaterialTheme.colorScheme.primary,
        useDarkTheme = LocalDarkMode.current,
        animate = true
    ) {
        content()
    }
}

@Composable
fun CommonSchemeTheme(content: @Composable () -> Unit) {
    val key by SchemeTheme.commonSchemeKey.collectAsStateWithLifecycle()

    SchemeTheme(key, content)
}

@Composable
expect fun SchemeThemeSystemProvider(scheme: ColorScheme, content: @Composable () -> Unit)

private fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return maxOf(fgLuminance, bgLuminance) / minOf(fgLuminance, bgLuminance)
}

private const val MinContrastRatio = 3f