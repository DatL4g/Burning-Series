package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import dev.datlag.burningseries.shared.ui.theme.image.PainterImage

@Composable
actual fun SchemeThemeSystemProvider(
    scheme: ColorScheme,
    content: @Composable () -> Unit
) {
    content()
}
