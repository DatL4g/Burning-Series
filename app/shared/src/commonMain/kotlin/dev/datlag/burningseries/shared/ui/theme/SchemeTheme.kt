package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import com.kmpalette.DominantColorState
import com.kmpalette.palette.graphics.Palette
import com.kmpalette.rememberPainterDominantColorState
import com.materialkolor.DynamicMaterialTheme
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.burningseries.model.common.scopeCatching
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.shared.LocalDarkMode
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.withIOContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

val <T : Any> DominantColorState<T>?.primary
    @Composable
    get() = this?.color ?: MaterialTheme.colorScheme.primary

val <T : Any> DominantColorState<T>?.onPrimary
    @Composable
    get() = this?.onColor ?: MaterialTheme.colorScheme.onPrimary

val Color.plainOnColor: Color
    get() = if (this.luminance() > 0.5F) {
        Color.Black
    } else {
        Color.White
    }

data object SchemeTheme {

    internal val commonSchemeKey = MutableStateFlow<Any?>(null)
    private val kache = InMemoryKache<Any, DominantColorState<Painter>>(
        maxSize = 25L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
    }

    internal fun get(key: Any) = scopeCatching {
        kache.getIfAvailable(key)
    }.getOrNull()

    internal suspend fun getOrPut(key: Any, fallback: DominantColorState<Painter>) = suspendCatching {
        kache.getIfAvailable(key)
    }.getOrNull() ?: suspendCatching {
        kache.put(key, fallback)
    }.getOrNull() ?: suspendCatching {
        kache.getOrPut(key) { fallback }
    }.getOrNull()

    @Composable
    fun create(
        key: Any?,
        defaultColor: Color? = null,
        defaultOnColor: Color? = null,
    ): Updater? {
        if (key == null) {
            return null
        }

        val onColor = defaultOnColor ?: remember(defaultColor) {
            defaultColor?.plainOnColor
        }
        val state = rememberSchemeThemeDominantColorState(
            key = key,
            defaultColor = defaultColor ?: MaterialTheme.colorScheme.primary,
            defaultOnColor = onColor ?: MaterialTheme.colorScheme.onPrimary,
        )
        val scope = rememberCoroutineScope()
        return remember(state, scope) {
            state?.let { Updater.State(scope, it) }
        }
    }

    fun setCommon(key: Any?) {
        commonSchemeKey.update { key }
    }

    sealed interface Updater {
        fun update(input: Painter?)

        data class State(
            private val scope: CoroutineScope,
            private val state: DominantColorState<Painter>
        ) : Updater {
            override fun update(input: Painter?) {
                if (input == null) {
                    return
                }

                scope.launchIO {
                    state.updateFrom(input)
                }
            }
        }

        data class Default(
            private val key: Any,
            private val scope: CoroutineScope,
        ) : Updater {
            override fun update(input: Painter?) {
                if (input == null) {
                    return
                }

                scope.launchIO {
                    val state = get(key) ?: return@launchIO
                    state.updateFrom(input)
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
    isSwatchValid: (Palette.Swatch) -> Boolean = { true },
    builder: Palette.Builder.() -> Unit = {},
): DominantColorState<Painter>? {
    if (key == null) {
        return null
    }

    val existingState = remember(key) {
        SchemeTheme.get(key)
    } ?: SchemeTheme.get(key)

    if (existingState != null) {
        return existingState
    }

    val fallbackState = rememberPainterDominantColorState(
        defaultColor = defaultColor,
        defaultOnColor = defaultOnColor,
        builder = builder,
        isSwatchValid = isSwatchValid,
        coroutineContext = ioDispatcher()
    )
    val state by produceState<DominantColorState<Painter>?>(null, key) {
        value = withIOContext {
            SchemeTheme.getOrPut(key, fallbackState) ?: fallbackState
        }
    }

    return remember(state) { state ?: fallbackState }
}

@Composable
fun rememberSchemeThemeDominantColorState(
    key: Any?,
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    defaultOnColor: Color = MaterialTheme.colorScheme.onPrimary,
    clearFilter: Boolean = false,
    applyMinContrast: Boolean = false,
    minContrastBackgroundColor: Color = Color.Transparent
): DominantColorState<Painter>? {
    return rememberSchemeThemeDominantColorState(
        key = key,
        defaultColor = defaultColor,
        defaultOnColor = defaultOnColor,
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
fun SchemeTheme(
    key: Any?,
    animate: Boolean = true,
    defaultColor: Color? = null,
    defaultOnColor: Color? = null,
    content: @Composable (SchemeTheme.Updater?) -> Unit
) {
    val onColor = defaultOnColor ?: remember(defaultColor) {
        defaultColor?.plainOnColor
    }
    val state = rememberSchemeThemeDominantColorState(
        key = key,
        defaultColor = defaultColor ?: MaterialTheme.colorScheme.primary,
        defaultOnColor = onColor ?: MaterialTheme.colorScheme.onPrimary,
    )
    val updater = SchemeTheme.create(key)

    DynamicMaterialTheme(
        seedColor = state?.color,
        animate = animate
    ) {
        content(updater)
    }
}

@Composable
fun CommonSchemeTheme(
    animate: Boolean = true,
    content: @Composable (SchemeTheme.Updater?) -> Unit
) {
    val key by SchemeTheme.commonSchemeKey.collectAsStateWithLifecycle()

    SchemeTheme(
        key = key,
        animate = animate,
        content = content
    )
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