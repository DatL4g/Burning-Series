package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.palette.graphics.Palette
import com.kmpalette.DominantColorState
import com.kmpalette.loader.ImageBitmapLoader
import com.kmpalette.rememberDominantColorState
import com.materialkolor.AnimatedDynamicMaterialTheme
import com.materialkolor.DynamicMaterialTheme
import dev.datlag.burningseries.shared.LocalDarkMode
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.withIOContext
import dev.datlag.burningseries.shared.ui.theme.image.PainterImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

data object SchemeTheme {

    internal val commonSchemeKey = MutableStateFlow<Any?>(null)
    internal val itemScheme = MutableStateFlow<Map<Any, Color?>>(emptyMap())

    internal var _state: DominantColorState<Painter>? = null
    internal val state: DominantColorState<Painter>
        get() = _state!!

    fun setCommon(key: Any?) {
        commonSchemeKey.update { key }
    }

    @Composable
    fun update(key: Any, input: Painter) {
        if (_state == null) {
            return
        }

        LaunchedEffect(key, input) {
            withIOContext {
                state.updateFrom(input)

                itemScheme.getAndUpdate {
                    it.toMutableMap().apply {
                        put(key, state.color)
                    }
                }
            }
        }
    }
}

internal data class PainterLoader(
    private val density: Density,
    private val layoutDirection: LayoutDirection
) : ImageBitmapLoader<Painter> {
    override suspend fun load(input: Painter): ImageBitmap {
        return PainterImage(input, density, layoutDirection).asBitmap()
    }
}

@Composable
public fun rememberPainterDominantColorState(
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    defaultOnColor: Color = MaterialTheme.colorScheme.onPrimary,
    density: Density = LocalDensity.current,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    cacheSize: Int = 0,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    isSwatchValid: (Palette.Swatch) -> Boolean = { true },
    builder: Palette.Builder.() -> Unit = {},
): DominantColorState<Painter> = rememberDominantColorState(
    loader = PainterLoader(density, layoutDirection),
    defaultColor = defaultColor,
    defaultOnColor = defaultOnColor,
    cacheSize = cacheSize,
    coroutineContext = coroutineContext,
    isSwatchValid = isSwatchValid,
    builder = builder
)

@Composable
fun SchemeTheme(key: Any?, content: @Composable () -> Unit) {
    if (SchemeTheme._state == null) {
        SchemeTheme._state = rememberPainterDominantColorState()
    }

    val color by remember(key) {
        SchemeTheme.itemScheme.map { it[key] }
    }.collectAsStateWithLifecycle(SchemeTheme.itemScheme.value[key])

    AnimatedDynamicMaterialTheme(
        seedColor = color ?: MaterialTheme.colorScheme.primary,
        useDarkTheme = LocalDarkMode.current
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
