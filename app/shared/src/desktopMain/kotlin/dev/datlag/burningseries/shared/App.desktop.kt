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
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyleManager

val LocalWindow = compositionLocalOf<ComposeWindow> { error("No window state provided") }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
actual fun SystemProvider(content: @Composable () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val backdrop = WindowBackdrop.Solid(backgroundColor)

    WindowStyle(
        backdropType = backdrop,
        frameStyle = WindowFrameStyle(
            borderColor = backgroundColor,
            titleBarColor = backgroundColor,
            captionColor = onBackgroundColor,
            cornerPreference = WindowCornerPreference.ROUNDED
        )
    )

    CompositionLocalProvider(
        LocalContextMenuRepresentation provides MaterialContextMenuRepresentation(colors = ContextMenuColors(MaterialTheme.colorScheme)),
        LocalTextContextMenu provides MaterialTextContextMenu,
    ) {
        content()
    }
}

@Composable
private fun WindowStyle(
    window: ComposeWindow = LocalWindow.current,
    isDarkTheme: Boolean = LocalDarkMode.current,
    backdropType: WindowBackdrop = WindowBackdrop.Default,
    frameStyle: WindowFrameStyle = WindowFrameStyle()
) {
    val manager = remember(isDarkTheme) { WindowStyleManager(
        window = window,
        isDarkTheme = isDarkTheme,
        backdropType = backdropType,
        frameStyle = frameStyle
    ) }

    LaunchedEffect(isDarkTheme) {
        manager.isDarkTheme = isDarkTheme
    }

    LaunchedEffect(backdropType) {
        manager.backdropType = backdropType
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