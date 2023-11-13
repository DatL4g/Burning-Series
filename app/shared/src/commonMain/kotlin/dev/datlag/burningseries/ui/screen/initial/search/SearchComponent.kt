package dev.datlag.burningseries.ui.screen.initial.search

import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface SearchComponent : Component {

    val searchState: StateFlow<SearchState>

    fun retryLoadingSearch(): Any?
}