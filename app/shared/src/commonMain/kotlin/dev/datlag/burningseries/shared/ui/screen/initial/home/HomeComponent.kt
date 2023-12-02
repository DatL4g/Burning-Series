package dev.datlag.burningseries.shared.ui.screen.initial.home

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.shared.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface HomeComponent : Component {

    val child: Value<ChildSlot<*, Component>>

    val homeState: StateFlow<HomeState>

    fun retryLoadingHome(): Any?
    fun itemClicked(config: HomeConfig)
}