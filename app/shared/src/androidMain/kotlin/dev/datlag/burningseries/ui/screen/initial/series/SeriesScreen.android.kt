package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import dev.datlag.burningseries.ui.KeyEventDispatcher
import dev.datlag.burningseries.ui.PIPActions
import dev.datlag.burningseries.ui.PIPEventDispatcher
import dev.datlag.burningseries.ui.PIPModeListener

@Composable
actual fun EnterSeriesScreen() {
    SideEffect {
        KeyEventDispatcher = { null }
        PIPEventDispatcher = { null }
        PIPModeListener = { }
        PIPActions = { null }
    }
}