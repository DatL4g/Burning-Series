package dev.datlag.burningseries.network

import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.network.state.SearchState

internal data object StateSaver {
    var homeState: HomeState = HomeState.Loading
    var searchState: SearchState = SearchState.Loading
}