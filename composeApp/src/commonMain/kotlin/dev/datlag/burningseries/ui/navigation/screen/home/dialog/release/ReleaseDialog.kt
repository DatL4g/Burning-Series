package dev.datlag.burningseries.ui.navigation.screen.home.dialog.release

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun ReleaseDialog(component: ReleaseComponent)

internal expect val DeviceIcon: ImageVector
    @Composable
    get