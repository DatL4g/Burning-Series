package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.dzirbel.contextmenu.MaterialContextMenuRepresentation
import dev.datlag.burningseries.color.createTheme
import dev.datlag.burningseries.shared.ContextMenuColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun SchemeThemeSystemProvider(
    scheme: ColorScheme,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContextMenuRepresentation provides MaterialContextMenuRepresentation(colors = ContextMenuColors(scheme))
    ) {
        content()
    }
}

@Composable
actual fun loadImageScheme(key: Any, painter: Painter) {
    if (!SchemeTheme.containsScheme(key)) {
        val awtImage = painter.toAwtImage(
            LocalDensity.current,
            LocalLayoutDirection.current
        )

        SchemeTheme.createColorScheme(key) {
            awtImage.createTheme()
        }
    }
}