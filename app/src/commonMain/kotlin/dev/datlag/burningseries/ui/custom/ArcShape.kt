package dev.datlag.burningseries.ui.custom

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class ArcShape(val sizePx: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(Path().apply {
            val sizePxAbs = abs(sizePx)

            moveTo(0F, 0F)
            lineTo(0F, size.height - sizePxAbs)
            quadraticBezierTo(size.width / 2, size.height + sizePxAbs, size.width, size.height - sizePxAbs)
            lineTo(size.width, 0F)
            close()
        })
    }
}