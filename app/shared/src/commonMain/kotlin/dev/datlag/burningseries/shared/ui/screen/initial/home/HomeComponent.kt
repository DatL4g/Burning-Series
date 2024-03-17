package dev.datlag.burningseries.shared.ui.screen.initial.home

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.SeriesHolderComponent
import kotlinx.coroutines.flow.StateFlow

interface HomeComponent : SeriesHolderComponent {

    val child: Value<ChildSlot<*, Component>>
    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    val homeState: StateFlow<HomeState>
    val release: StateFlow<Release?>

    val searchState: StateFlow<SearchState>
    val searchItems: StateFlow<List<Genre.Item>>

    val onDeviceReachable: StateFlow<Boolean>

    fun retryLoadingHome(): Any?
    fun itemClicked(config: HomeConfig)
    fun showDialog(config: DialogConfig)
    fun searchQuery(text: String)
    fun retryLoadingSearch()
}