package dev.datlag.burningseries.ui.screen.initial.search

import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface SearchComponent : Component {

    val searchState: StateFlow<SearchState>
    val genres: StateFlow<List<Genre>>
    val canLoadMoreGenres: StateFlow<Boolean>

    val searchItems: StateFlow<List<Genre.Item>>

    fun retryLoadingSearch(): Any?
    fun loadMoreGenres(): Any?
    fun searchQuery(text: String)
}