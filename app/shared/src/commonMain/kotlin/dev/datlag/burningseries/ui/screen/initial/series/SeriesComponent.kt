package dev.datlag.burningseries.ui.screen.initial.series

import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface SeriesComponent : Component {

    val initialTitle: String
    val initialCoverHref: String?

    val seriesState: StateFlow<SeriesState>

    fun retryLoadingSeries(): Any?

    fun goBack()
}