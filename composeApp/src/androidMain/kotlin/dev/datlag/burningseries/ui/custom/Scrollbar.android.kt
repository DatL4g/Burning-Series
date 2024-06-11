package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun VerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    style: ScrollbarStyle
) { }

@Composable
actual fun HorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    style: ScrollbarStyle
) { }

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyGridState,
): ScrollbarAdapter = remember { DEFAULT_SCROLLBAR_ADAPTER }

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyListState
): ScrollbarAdapter = remember {
    DEFAULT_SCROLLBAR_ADAPTER
}

actual interface ScrollbarAdapter {
    actual val scrollOffset: Double
    actual val contentSize: Double
    actual val viewportSize: Double

    actual suspend fun scrollTo(scrollOffset: Double)
}

private val DEFAULT_SCROLLBAR_ADAPTER = object : ScrollbarAdapter {
    override val scrollOffset: Double
        get() = 0.0
    override val contentSize: Double
        get() = 0.0
    override val viewportSize: Double
        get() = 0.0

    override suspend fun scrollTo(scrollOffset: Double) { }
}

@Composable
actual fun localScrollbarStyle(): ScrollbarStyle {
    return ScrollbarStyle(
        minimalHeight = 16.dp,
        thickness = 8.dp,
        shape = RoundedCornerShape(4.dp),
        hoverDurationMillis = 300,
        unhoverColor = Color.Black.copy(alpha = 0.12f),
        hoverColor = Color.Black.copy(alpha = 0.50f)
    )
}