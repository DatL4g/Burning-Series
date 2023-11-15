package dev.datlag.burningseries.ui.screen.initial.search

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface SearchComponent : Component {

    val searchState: StateFlow<SearchState>
    val genres: StateFlow<List<Genre>>
    val canLoadMoreGenres: StateFlow<Boolean>

    val searchItems: StateFlow<List<Genre.Item>>

    val child: Value<ChildSlot<*, Component>>

    fun retryLoadingSearch(): Any?
    fun loadMoreGenres(): Any?
    fun searchQuery(text: String)
    fun itemClicked(config: SearchConfig)
}