package dev.datlag.burningseries.ui.custom

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

class TintPainter(
    private val painter: Painter,
    private val tint: Color
): Painter() {
    override val intrinsicSize: Size = painter.intrinsicSize

    override fun DrawScope.onDraw() {
        with(painter) {
            draw(
                size = Size(
                    width = painter.intrinsicSize.width / 2F,
                    height = painter.intrinsicSize.height / 2F
                ),
                colorFilter = ColorFilter.tint(tint)
            )
        }
    }

}