package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import com.dzirbel.contextmenu.MaterialContextMenuRepresentation
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