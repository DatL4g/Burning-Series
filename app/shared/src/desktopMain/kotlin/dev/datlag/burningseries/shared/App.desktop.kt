package dev.datlag.burningseries.shared

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import com.dzirbel.contextmenu.ContextMenuColors
import com.dzirbel.contextmenu.MaterialContextMenuRepresentation
import com.dzirbel.contextmenu.MaterialTextContextMenu

val LocalWindow = compositionLocalOf<ComposeWindow> { error("No window state provided") }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
actual fun SystemProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalContextMenuRepresentation provides MaterialContextMenuRepresentation(colors = ContextMenuColors(MaterialTheme.colorScheme)),
        LocalTextContextMenu provides MaterialTextContextMenu,
    ) {
        content()
    }
}

@Composable
fun ContextMenuColors(scheme: ColorScheme = MaterialTheme.colorScheme) = ContextMenuColors(
    surface = scheme.surface,
    text = scheme.onSurface,
    icon = scheme.onSurface,
    divider = scheme.onSurfaceVariant,
    shortcutText = scheme.onSurfaceVariant
)

@Composable
actual fun isTv(): Boolean {
    return false
}