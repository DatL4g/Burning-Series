package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(adapter: ScrollbarAdapter, modifier: Modifier) { }

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyGridState,
): ScrollbarAdapter = remember {
    object : ScrollbarAdapter {
        override val scrollOffset: Double
            get() = 0.0
        override val contentSize: Double
            get() = 0.0
        override val viewportSize: Double
            get() = 0.0

        override suspend fun scrollTo(scrollOffset: Double) { }
    }
}

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyListState
): ScrollbarAdapter = remember {
    object : ScrollbarAdapter {
        override val scrollOffset: Double
            get() = 0.0
        override val contentSize: Double
            get() = 0.0
        override val viewportSize: Double
            get() = 0.0

        override suspend fun scrollTo(scrollOffset: Double) { }
    }
}

actual interface ScrollbarAdapter {
    actual val scrollOffset: Double
    actual val contentSize: Double
    actual val viewportSize: Double
    actual suspend fun scrollTo(scrollOffset: Double)
}