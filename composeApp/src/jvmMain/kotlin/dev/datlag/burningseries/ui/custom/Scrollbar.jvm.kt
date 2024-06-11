package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    style: ScrollbarStyle
) {
    androidx.compose.foundation.VerticalScrollbar(
        adapter = adapter,
        modifier = modifier,
        style = androidx.compose.foundation.ScrollbarStyle(
            minimalHeight = style.minimalHeight,
            thickness = style.thickness,
            shape = style.shape,
            hoverDurationMillis = style.hoverDurationMillis,
            unhoverColor = style.unhoverColor,
            hoverColor = style.hoverColor
        )
    )
}

@Composable
actual fun HorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    style: ScrollbarStyle
) {
    androidx.compose.foundation.HorizontalScrollbar(
        adapter = adapter,
        modifier = modifier,
        style = androidx.compose.foundation.ScrollbarStyle(
            minimalHeight = style.minimalHeight,
            thickness = style.thickness,
            shape = style.shape,
            hoverDurationMillis = style.hoverDurationMillis,
            unhoverColor = style.unhoverColor,
            hoverColor = style.hoverColor
        )
    )
}

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyGridState,
): ScrollbarAdapter = remember(scrollState) {
    androidx.compose.foundation.ScrollbarAdapter(scrollState)
}

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyListState
): ScrollbarAdapter = remember(scrollState) {
    androidx.compose.foundation.ScrollbarAdapter(scrollState)
}

actual typealias ScrollbarAdapter = androidx.compose.foundation.v2.ScrollbarAdapter

@Composable
actual fun localScrollbarStyle(): ScrollbarStyle {
    val current = LocalScrollbarStyle.current

    return remember {
        ScrollbarStyle(
            minimalHeight = current.minimalHeight,
            thickness = current.thickness,
            shape = current.shape,
            hoverDurationMillis = current.hoverDurationMillis,
            unhoverColor = current.unhoverColor,
            hoverColor = current.hoverColor
        )
    }
}