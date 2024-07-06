package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
expect fun VerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    style: ScrollbarStyle = localScrollbarStyle()
)

@Composable
expect fun HorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    style: ScrollbarStyle = localScrollbarStyle()
)

@Composable
expect fun rememberScrollbarAdapter(
    scrollState: LazyGridState,
): ScrollbarAdapter

@Composable
expect fun rememberScrollbarAdapter(
    scrollState: LazyListState,
): ScrollbarAdapter

expect interface ScrollbarAdapter {
    val scrollOffset: Double
    val contentSize: Double
    val viewportSize: Double

    suspend fun scrollTo(scrollOffset: Double)
}

@Immutable
data class ScrollbarStyle(
    val minimalHeight: Dp,
    val thickness: Dp,
    val shape: Shape,
    val hoverDurationMillis: Int,
    val unhoverColor: Color,
    val hoverColor: Color
)

@Composable
expect fun localScrollbarStyle(): ScrollbarStyle