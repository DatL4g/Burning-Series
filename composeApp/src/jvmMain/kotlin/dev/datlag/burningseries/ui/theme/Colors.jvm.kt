package dev.datlag.burningseries.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun Colors.dynamicDark(): ColorScheme {
    return getDarkScheme()
}

@Composable
actual fun Colors.dynamicLight(): ColorScheme {
    return getLightScheme()
}