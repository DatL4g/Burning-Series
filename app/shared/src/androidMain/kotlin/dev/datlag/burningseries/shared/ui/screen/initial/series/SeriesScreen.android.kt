package dev.datlag.burningseries.shared.ui.screen.initial.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import dev.datlag.burningseries.shared.ui.KeyEventDispatcher
import dev.datlag.burningseries.shared.ui.PIPActions
import dev.datlag.burningseries.shared.ui.PIPEventDispatcher
import dev.datlag.burningseries.shared.ui.PIPModeListener

@Composable
actual fun EnterSeriesScreen() {
    SideEffect {
        KeyEventDispatcher = { null }
        PIPEventDispatcher = { null }
        PIPModeListener = { }
        PIPActions = { null }
    }
}