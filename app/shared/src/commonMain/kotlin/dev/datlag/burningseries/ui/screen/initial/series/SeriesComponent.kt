package dev.datlag.burningseries.ui.screen.initial.series

import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface SeriesComponent : Component {

    val seriesState: StateFlow<SeriesState>

    val title: StateFlow<String>
    val href: StateFlow<String>
    val coverHref: StateFlow<String?>

    fun retryLoadingSeries(): Any?

    fun goBack()
}