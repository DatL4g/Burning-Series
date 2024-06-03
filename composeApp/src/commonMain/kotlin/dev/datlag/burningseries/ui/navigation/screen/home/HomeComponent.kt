package dev.datlag.burningseries.ui.navigation.screen.home

import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface HomeComponent : Component {
    val home: StateFlow<HomeState>
}