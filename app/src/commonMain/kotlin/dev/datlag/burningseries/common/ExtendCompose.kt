package dev.datlag.burningseries.common

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

val Color.Companion.SemiBlack: Color
    get() = Color.Black.copy(alpha = 0.5F)

val Color.Companion.Success: Color
    get() = Color(58, 206, 162)

@Composable
fun ImageVector.painter(tint: Color = this.tintColor, blendMode: BlendMode = this.tintBlendMode): Painter {
    return rememberVectorPainter(
        this.defaultWidth,
        this.defaultHeight,
        this.viewportWidth,
        this.viewportHeight,
        this.name,
        tint,
        blendMode,
        this.autoMirror
    ) { _, _ ->
        RenderVectorGroup(this.root)
    }
}