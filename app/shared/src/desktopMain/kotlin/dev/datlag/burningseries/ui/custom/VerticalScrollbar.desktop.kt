package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(adapter: ScrollbarAdapter, modifier: Modifier) {
    androidx.compose.foundation.VerticalScrollbar(
        adapter = adapter,
        modifier = modifier
    )
}

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyGridState,
): ScrollbarAdapter = remember(scrollState) {
    androidx.compose.foundation.ScrollbarAdapter(scrollState)
}

actual typealias ScrollbarAdapter = androidx.compose.foundation.v2.ScrollbarAdapter