package dev.datlag.burningseries.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import com.kmpalette.DominantColorState
import com.kmpalette.palette.graphics.Palette
import com.kmpalette.rememberPainterDominantColorState
import com.materialkolor.DynamicMaterialTheme
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.burningseries.common.plainOnColor
import dev.datlag.burningseries.model.coroutines.Executor
import dev.datlag.tooling.async.scopeCatching
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.compose.launchDefault
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

data object SchemeTheme {

    internal val executor = Executor()
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

    sealed interface Updater {
        fun update(input: Painter?)
        fun update(color: Color?) = update(color?.let(::ColorPainter))

        data class State(
            private val scope: CoroutineScope,
            private val state: DominantColorState<Painter>
        ) : Updater {
            override fun update(input: Painter?) {
                if (input == null) {
                    return
                }

                scope.launchIO {
                    executor.enqueue {
                        state.updateFrom(input)
                    }
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
                    executor.enqueue {
                        val state = get(key) ?: return@enqueue
                        state.updateFrom(input)
                    }
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
    animate: Boolean = false,
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

private fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return maxOf(fgLuminance, bgLuminance) / minOf(fgLuminance, bgLuminance)
}

private const val MinContrastRatio = 3f