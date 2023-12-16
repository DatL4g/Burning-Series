package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.common.drawPathFromSvgData

data object MaterialSymbols {
    @Composable
    fun rememberDeployedCodeAlert(fillColor: Color = LocalContentColor.current): ImageVector {
        return remember {
            ImageVector.Builder(
                name = "deployed_code_alert",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(fillColor),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero
                ) {
                    drawPathFromSvgData("M11 2.775C11.3167 2.59167 11.65 2.5 12 2.5C12.35 2.5 12.6833 2.59167 13 2.775L20 6.8C20.3167 6.98333 20.5625 7.225 20.7375 7.525C20.9125 7.825 21 8.15833 21 8.525V12.775C20.7 12.5583 20.3833 12.3708 20.05 12.2125C19.7167 12.0542 19.3667 11.9167 19 11.8V9.6L15.4 11.675C14.4667 11.9083 13.6208 12.3042 12.8625 12.8625C12.1042 13.4208 11.4833 14.1 11 14.9V13.075L5 9.6V16.45L10.05 19.375C10.1167 19.9083 10.2417 20.4208 10.425 20.9125C10.6083 21.4042 10.8417 21.8667 11.125 22.3C11.0917 22.2833 11.0708 22.2708 11.0625 22.2625C11.0542 22.2542 11.0333 22.2417 11 22.225L4 18.2C3.68333 18.0167 3.4375 17.775 3.2625 17.475C3.0875 17.175 3 16.8417 3 16.475V8.525C3 8.15833 3.0875 7.825 3.2625 7.525C3.4375 7.225 3.68333 6.98333 4 6.8L11 2.775ZM12 4.5L6.075 7.925L12 11.35L17.925 7.925L12 4.5ZM17 23.5C15.6167 23.5 14.4375 23.0125 13.4625 22.0375C12.4875 21.0625 12 19.8833 12 18.5C12 17.1167 12.4875 15.9375 13.4625 14.9625C14.4375 13.9875 15.6167 13.5 17 13.5C18.3833 13.5 19.5625 13.9875 20.5375 14.9625C21.5125 15.9375 22 17.1167 22 18.5C22 19.8833 21.5125 21.0625 20.5375 22.0375C19.5625 23.0125 18.3833 23.5 17 23.5ZM17 21.5C17.1333 21.5 17.25 21.45 17.35 21.35C17.45 21.25 17.5 21.1333 17.5 21C17.5 20.8667 17.45 20.75 17.35 20.65C17.25 20.55 17.1333 20.5 17 20.5C16.8667 20.5 16.75 20.55 16.65 20.65C16.55 20.75 16.5 20.8667 16.5 21C16.5 21.1333 16.55 21.25 16.65 21.35C16.75 21.45 16.8667 21.5 17 21.5ZM16.5 19.5H17.5V15.5H16.5V19.5Z")
                }
            }.build()
        }
    }
}