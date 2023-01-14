package dev.datlag.burningseries.ui.screen.home

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.common.isTelevision
import dev.datlag.burningseries.common.isTv
import dev.datlag.burningseries.other.Orientation

@Composable
actual fun gridCellSize(): GridCells {
    val orientation = LocalOrientation.current


    return if (isTv() || orientation == Orientation.LANDSCAPE) {
        GridCells.Adaptive(200.dp)
    } else {
        GridCells.Fixed(2)
    }
}