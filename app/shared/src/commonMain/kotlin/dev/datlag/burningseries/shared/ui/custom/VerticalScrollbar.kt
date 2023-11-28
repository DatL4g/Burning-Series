package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
expect fun VerticalScrollbar(adapter: ScrollbarAdapter, modifier: Modifier = Modifier)

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