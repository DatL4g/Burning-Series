package dev.datlag.burningseries.shared.ui.theme.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan

data class DiagonalShape(
    internal val angle: Float = 0F,
    internal val position: POSITION = POSITION.TOP
) : Shape {

    private val direction: DIRECTION
        get() = if (angle > 0) DIRECTION.LEFT else DIRECTION.RIGHT

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(Path().apply {
            val angleAbs = abs(angle)
            val perpendicularHeight = (size.width * tan(angleAbs / 180 * PI)).toFloat()

            when (position) {
                is POSITION.START -> {
                    when (direction) {
                        is DIRECTION.LEFT -> {
                            this.moveTo(perpendicularHeight, 0F)
                            this.lineTo(size.width, 0F)
                            this.lineTo(size.width, size.height)
                            this.lineTo(0F, size.height)
                        }
                        is DIRECTION.RIGHT -> {
                            this.moveTo(0F, 0F)
                            this.lineTo(size.width, 0F)
                            this.lineTo(size.width, size.height)
                            this.lineTo(perpendicularHeight, size.height)
                        }
                    }
                }
                is POSITION.TOP -> {
                    when (direction) {
                        is DIRECTION.LEFT -> {
                            this.moveTo(size.width, size.height)
                            this.lineTo(size.width, perpendicularHeight)
                            this.lineTo(0F, 0F)
                            this.lineTo(0F, size.height)
                        }
                        is DIRECTION.RIGHT -> {
                            this.moveTo(size.width, size.height)
                            this.lineTo(size.width, 0F)
                            this.lineTo(0F, perpendicularHeight)
                            this.lineTo(0F, size.height)
                        }
                    }
                }
                is POSITION.END -> {
                    when (direction) {
                        is DIRECTION.LEFT -> {
                            this.moveTo(0F, 0F)
                            this.lineTo(size.width, 0F)
                            this.lineTo(size.width - perpendicularHeight, size.height)
                            this.lineTo(0F, size.height)
                        }
                        is DIRECTION.RIGHT -> {
                            this.moveTo(0F, 0F)
                            this.lineTo(size.width - perpendicularHeight, 0F)
                            this.lineTo(size.width, size.height)
                            this.lineTo(0F, size.height)
                        }
                    }
                }
                is POSITION.BOTTOM -> {
                    when (direction) {
                        is DIRECTION.LEFT -> {
                            this.moveTo(0F, 0F)
                            this.lineTo(size.width, 0F)
                            this.lineTo(size.width, size.height - perpendicularHeight)
                            this.lineTo(0F, size.height)
                        }
                        is DIRECTION.RIGHT -> {
                            this.moveTo(size.width, size.height)
                            this.lineTo(0F, size.height - perpendicularHeight)
                            this.lineTo(0F, 0F)
                            this.lineTo(size.width, 0F)
                        }
                    }
                }
            }

            this.close()
        })
    }

    sealed interface POSITION {
        data object START : POSITION
        data object TOP : POSITION
        data object END : POSITION
        data object BOTTOM : POSITION
    }

    private sealed interface DIRECTION {
        data object LEFT : DIRECTION
        data object RIGHT : DIRECTION
    }
}