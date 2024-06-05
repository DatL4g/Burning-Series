package dev.datlag.burningseries.ui.navigation.screen.home

import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.StateFlow

interface HomeComponent : Component {
    val home: StateFlow<HomeState>
    val search: StateFlow<SearchState>

    fun details(data: SeriesData)
    fun retryLoadingSearch()
}