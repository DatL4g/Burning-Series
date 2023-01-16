package dev.datlag.burningseries.ui.screen.favorite

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.common.toFlow
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.ui.custom.SearchAppBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class FavoriteScreenComponent(
    componentContext: ComponentContext,
    override val onGoBack: () -> Unit,
    private val onSeries: (String, SeriesInitialInfo) -> Unit,
    override val di: DI
) : FavoriteComponent, ComponentContext by componentContext {

    private val db: BurningSeriesDB by di.instance()

    override val imageDir: File by di.instance("ImageDir")
    override val favorites = db.burningSeriesQueries.selectFavorites().asFlow().mapToList(Dispatchers.IO)

    private val _searchAppBarState: MutableValue<SearchAppBarState> = MutableValue(SearchAppBarState.CLOSED)
    override val searchAppBarState: Value<SearchAppBarState> = _searchAppBarState

    private val _searchText = MutableValue(String())
    override val searchText: Value<String> = _searchText

    override val searchItems: Flow<List<DBSeries>> = combine(searchText.toFlow(), favorites) { t1, t2 ->
        if (t1.isEmpty()) {
            emptyList()
        } else {
            t2.map {
                when {
                    it.title.equals(t1, true) -> it to 1.0
                    it.title.startsWith(t1, true) -> it to 0.95
                    it.title.contains(t1, true) -> it to 0.9
                    else -> it to JaroWinkler.distance(it.title, t1)
                }
            }.filter { it.second > 0.85 }.sortedByDescending { it.second }.map { it.first }
        }
    }

    override fun onSeriesClicked(href: String, initialInfo: SeriesInitialInfo) {
        onSeries(href, initialInfo)
    }

    override fun openSearchBar() {
        _searchAppBarState.value = SearchAppBarState.OPENED
    }

    override fun closeSearchBar() {
        _searchAppBarState.value = SearchAppBarState.CLOSED
    }

    override fun updateSearchText(value: String) {
        _searchText.value = value
    }

    @Composable
    override fun render() {
        FavoriteScreen(this)
    }
}