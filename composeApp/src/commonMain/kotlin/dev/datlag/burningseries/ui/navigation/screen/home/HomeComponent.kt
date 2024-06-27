package dev.datlag.burningseries.ui.navigation.screen.home

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.ExtendedSeries
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.github.UserAndReleaseState
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface HomeComponent : Component {
    val home: StateFlow<HomeState>
    val search: StateFlow<SearchState>
    val showFavorites: StateFlow<Boolean>
    val favorites: StateFlow<ImmutableCollection<ExtendedSeries>>
    val language: Flow<Language?>

    val githubState: Flow<UserAndReleaseState>
    val appVersion: String?
    val displayRelease: StateFlow<Boolean>

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    fun details(data: SeriesData) = details(data, null)
    fun details(data: SeriesData, language: Language?)
    fun search(query: String?)
    fun retryLoadingSearch()
    fun toggleFavorites()
    fun settings()
    fun release(release: UserAndRelease.Release)
}