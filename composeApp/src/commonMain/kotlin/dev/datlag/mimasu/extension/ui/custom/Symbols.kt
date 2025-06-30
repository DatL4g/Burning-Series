package dev.datlag.mimasu.extension.ui.custom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

data object Symbols {

    private var _Github: ImageVector? = null

    public val Github: ImageVector
        get() {
            return _Github ?: ImageVector.Builder(
                name = "Github",
                defaultWidth = 300.dp,
                defaultHeight = 300.dp,
                viewportWidth = 300f,
                viewportHeight = 300f
            ).apply {
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(150.001f, 0f)
                    curveTo(67.1687f, 0f, 0f, 68.8559f, 0f, 153.798f)
                    curveTo(0f, 221.749f, 42.9799f, 279.399f, 102.58f, 299.736f)
                    curveTo(110.077f, 301.159f, 112.829f, 296.399f, 112.829f, 292.337f)
                    curveTo(112.829f, 288.67f, 112.69f, 276.554f, 112.625f, 263.703f)
                    curveTo(70.8946f, 273.007f, 62.089f, 245.557f, 62.089f, 245.557f)
                    curveTo(55.2656f, 227.78f, 45.4341f, 223.053f, 45.4341f, 223.053f)
                    curveTo(31.8245f, 213.508f, 46.4599f, 213.704f, 46.4599f, 213.704f)
                    curveTo(61.5227f, 214.786f, 69.4539f, 229.555f, 69.4539f, 229.555f)
                    curveTo(82.8325f, 253.065f, 104.545f, 246.268f, 113.105f, 242.338f)
                    curveTo(114.451f, 232.398f, 118.338f, 225.61f, 122.628f, 221.772f)
                    curveTo(89.3107f, 217.883f, 54.2869f, 204.696f, 54.2869f, 145.765f)
                    curveTo(54.2869f, 128.974f, 60.1466f, 115.254f, 69.7421f, 104.483f)
                    curveTo(68.1846f, 100.607f, 63.0503f, 84.9671f, 71.1952f, 63.7826f)
                    curveTo(71.1952f, 63.7826f, 83.7914f, 59.6492f, 112.456f, 79.5475f)
                    curveTo(124.421f, 76.1398f, 137.254f, 74.4309f, 150.001f, 74.3723f)
                    curveTo(162.749f, 74.4309f, 175.591f, 76.1398f, 187.579f, 79.5475f)
                    curveTo(216.209f, 59.6492f, 228.787f, 63.7826f, 228.787f, 63.7826f)
                    curveTo(236.952f, 84.9671f, 231.815f, 100.607f, 230.258f, 104.483f)
                    curveTo(239.876f, 115.254f, 245.696f, 128.974f, 245.696f, 145.765f)
                    curveTo(245.696f, 204.836f, 210.605f, 217.843f, 177.203f, 221.65f)
                    curveTo(182.583f, 226.423f, 187.377f, 235.782f, 187.377f, 250.131f)
                    curveTo(187.377f, 270.71f, 187.203f, 287.271f, 187.203f, 292.337f)
                    curveTo(187.203f, 296.43f, 189.904f, 301.226f, 197.507f, 299.715f)
                    curveTo(257.075f, 279.356f, 300f, 221.726f, 300f, 153.798f)
                    curveTo(300f, 68.8559f, 232.841f, 0f, 150.001f, 0f)
                    close()
                }
            }.build().also {
                _Github = it
            }
        }
}