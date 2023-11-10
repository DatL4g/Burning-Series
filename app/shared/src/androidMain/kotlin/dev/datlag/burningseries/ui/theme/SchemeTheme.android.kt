package dev.datlag.burningseries.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import dev.datlag.burningseries.color.createTheme
import dev.datlag.burningseries.color.utils.ThemeUtils
import dev.datlag.burningseries.ui.theme.image.PainterImage

@Composable
actual fun SchemeThemeSystemProvider(
    scheme: ColorScheme,
    content: @Composable () -> Unit
) {
    content()
}

@Composable
actual fun loadImageScheme(key: Any, painter: Painter) {
    if (!SchemeTheme.containsScheme(key)) {
        val density = LocalDensity.current
        val layout = LocalLayoutDirection.current

        SchemeTheme.createColorScheme(key) {
            val bitmap = PainterImage(
                painter,
                density,
                layout
            ).asBitmap()

            ThemeUtils.intArrayToTheme(
                bitmap.toPixelMap().buffer
            )
        }
    }
}
