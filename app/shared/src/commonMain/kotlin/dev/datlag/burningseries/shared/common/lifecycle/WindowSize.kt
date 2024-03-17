package dev.datlag.burningseries.shared.common.lifecycle

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun calculateWindowWidthSize(): WindowSize {
    val sizeClass = calculateWindowSizeClass()

    return when (sizeClass.widthSizeClass) {
        WindowWidthSizeClass.Medium -> WindowSize.Medium
        WindowWidthSizeClass.Expanded -> when (sizeClass.heightSizeClass) {
            WindowHeightSizeClass.Compact -> WindowSize.Medium
            else -> WindowSize.Expanded
        }
        else -> WindowSize.Compact
    }
}

sealed interface WindowSize {
    data object Compact : WindowSize
    data object Medium : WindowSize
    data object Expanded : WindowSize
}