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
import dev.datlag.burningseries.LocalDI
import dev.datlag.burningseries.common.plainOnColor
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.model.coroutines.Executor
import dev.datlag.burningseries.settings.Settings
import dev.datlag.tooling.Platform
import dev.datlag.tooling.async.scopeCatching
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.compose.launchDefault
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.toTypography
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import io.github.alexzhirkevich.qrose.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.kodein.di.instance
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

data object SchemeTheme {

    internal val executor = Executor()
    private val kache = InMemoryKache<Any, DominantColorState<Painter>>(
        maxSize = 25L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
    }
    private val imageBytes = InMemoryKache<Any, ByteArray>(
        maxSize = 5L * 1024 * 1024
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

    internal fun getByteArray(key: Any) = scopeCatching {
        val usingKey = when (key) {
            is SeriesData -> key.source
            else -> key
        }

        imageBytes.getIfAvailable(usingKey)
    }.getOrNull()

    internal suspend fun getOrPutByteArray(key: Any, fallback: ByteArray = ByteArray(0)): ByteArray? {
        val usingKey = when (key) {
            is SeriesData -> key.source
            else -> key
        }

        return suspendCatching {
            imageBytes.getIfAvailable(usingKey)
        }.getOrNull() ?: suspendCatching {
            if (fallback.isNotEmpty()) {
                imageBytes.put(usingKey, fallback)
            } else {
                null
            }
        }.getOrNull() ?: suspendCatching {
            imageBytes.getOrPut(usingKey) {
                if (fallback.isNotEmpty()) {
                    fallback
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    internal suspend fun setByteArray(key: Any, data: ByteArray) = suspendCatching {
        val usingKey = when (key) {
            is SeriesData -> key.source
            else -> key
        }

        if (data.isNotEmpty()) {
            imageBytes.put(usingKey, data)
        } else {
            null
        }
    }.isSuccess

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
            defaultColor = defaultColor ?: Platform.colorScheme().primary,
            defaultOnColor = onColor ?: Platform.colorScheme().onPrimary,
        )
        val scope = rememberCoroutineScope()
        return remember(state, scope) {
            state?.let { Updater.State(key, scope, it) }
        }
    }

    sealed interface Updater {
        fun update(input: Painter?)
        fun update(color: Color?) = update(color?.let(::ColorPainter))

        data class State(
            private val key: Any,
            private val scope: CoroutineScope,
            private val state: DominantColorState<Painter>
        ) : Updater {
            override fun update(input: Painter?) {
                if (input == null) {
                    return
                }

                scope.launchIO {
                    executor.enqueue {
                        setByteArray(key, input.toByteArray(
                            width = input.intrinsicSize.width.roundToInt(),
                            height = input.intrinsicSize.height.roundToInt()
                        ))

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
                        setByteArray(key, input.toByteArray(
                            width = input.intrinsicSize.width.roundToInt(),
                            height = input.intrinsicSize.height.roundToInt()
                        ))

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
    defaultColor: Color = Platform.colorScheme().primary,
    defaultOnColor: Color = Platform.colorScheme().onPrimary,
    isSwatchValid: (Palette.Swatch) -> Boolean = { true },
    builder: Palette.Builder.() -> Unit = {},
): DominantColorState<Painter>? {
    if (key == null) {
        return null
    }

    val usingKey = when (key) {
        is SeriesData -> key.source
        else -> key
    }

    val existingState = remember(usingKey) {
        SchemeTheme.get(usingKey)
    } ?: SchemeTheme.get(usingKey)

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
    val state by produceState<DominantColorState<Painter>?>(null, usingKey) {
        value = withIOContext {
            SchemeTheme.getOrPut(usingKey, fallbackState) ?: fallbackState
        }
    }

    return remember(state) { state ?: fallbackState }
}

@Composable
fun rememberSchemeThemeDominantColorState(
    key: Any?,
    defaultColor: Color = Platform.colorScheme().primary,
    defaultOnColor: Color = Platform.colorScheme().onPrimary,
    clearFilter: Boolean = false,
    applyMinContrast: Boolean = false,
    minContrastBackgroundColor: Color = Color.Transparent
): DominantColorState<Painter>? {
    val usingKey = when (key) {
        is SeriesData -> key.source
        else -> key
    }

    return rememberSchemeThemeDominantColorState(
        key = usingKey,
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
    val usingKey = when (key) {
        is SeriesData -> key.source
        else -> key
    }
    val state = rememberSchemeThemeDominantColorState(
        key = usingKey,
        defaultColor = defaultColor ?: Platform.colorScheme().primary,
        defaultOnColor = onColor ?: Platform.colorScheme().onPrimary,
    )
    val updater = SchemeTheme.create(usingKey)
    val appSettings by LocalDI.current.instance<Settings.PlatformAppSettings>()

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