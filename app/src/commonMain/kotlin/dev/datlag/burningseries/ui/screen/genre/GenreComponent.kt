package dev.datlag.burningseries.ui.screen.genre

import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.custom.SearchAppBarState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface GenreComponent : Component {

    val genre: Flow<Genre?>
    val onGoBack: () -> Unit
    val onSeriesClicked: (String, SeriesInitialInfo) -> Unit

    val searchText: Value<String>
    val searchAppBarState: Value<SearchAppBarState>
    val searchItems: Flow<List<Genre.Item>>

    fun nextGenre()
    fun previousGenre()

    fun openSearchBar()

    fun closeSearchBar()

    fun updateSearchText(value: String)
}