package dev.datlag.burningseries.common

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

val Color.Companion.SemiBlack: Color
    get() = Color.Black.copy(alpha = 0.5F)

val Color.Companion.Success: Color
    get() = Color(58, 206, 162)