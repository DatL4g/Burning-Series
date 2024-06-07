package dev.datlag.burningseries.ui.navigation.screen.medium

import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface MediumComponent : Component {
    val initialSeriesData: SeriesData

    val seriesState: StateFlow<SeriesState>
}