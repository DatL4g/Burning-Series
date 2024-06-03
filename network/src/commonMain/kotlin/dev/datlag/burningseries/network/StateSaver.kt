package dev.datlag.burningseries.network

import dev.datlag.burningseries.network.state.HomeState

data object StateSaver {
    var homeState: HomeState = HomeState.Loading
}