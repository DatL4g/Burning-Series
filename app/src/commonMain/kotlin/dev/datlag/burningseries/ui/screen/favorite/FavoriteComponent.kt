package dev.datlag.burningseries.ui.screen.favorite

import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.ui.custom.SearchAppBarState
import dev.datlag.burningseries.ui.screen.SeriesItemComponent
import kotlinx.coroutines.flow.Flow

interface FavoriteComponent : SeriesItemComponent {

    val onGoBack: () -> Unit

    val favorites: Flow<List<DBSeries>>

    val searchText: Value<String>
    val searchAppBarState: Value<SearchAppBarState>
    val searchItems: Flow<List<DBSeries>>

    fun openSearchBar()

    fun closeSearchBar()

    fun updateSearchText(value: String)
}