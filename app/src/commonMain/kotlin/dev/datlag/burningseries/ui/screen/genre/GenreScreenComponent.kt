package dev.datlag.burningseries.ui.screen.genre

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.network.repository.GenreRepository
import org.kodein.di.DI
import org.kodein.di.instance
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.custom.SearchAppBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GenreScreenComponent(
    componentContext: ComponentContext,
    override val onGoBack: () -> Unit,
    override val onSeriesClicked: (String, SeriesInitialInfo) -> Unit,
    override val di: DI
) : GenreComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val genreRepo: GenreRepository by di.instance()
    override val genre = genreRepo.currentGenre

    private val _searchAppBarState: MutableValue<SearchAppBarState> = MutableValue(SearchAppBarState.CLOSED)
    override val searchAppBarState: Value<SearchAppBarState> = _searchAppBarState

    private val _searchText = MutableValue(String())
    override val searchText: Value<String> = _searchText
    private var searchJob: Job? = null
    override val searchItems: Flow<List<Genre.Item>> = genreRepo.searchItems

    override fun nextGenre() {
        scope.launch(Dispatchers.IO) {
            genreRepo.nextGenre()
        }
    }
    override fun previousGenre() {
        scope.launch(Dispatchers.IO) {
            genreRepo.previousGenre()
        }
    }

    override fun openSearchBar() {
        _searchAppBarState.value = SearchAppBarState.OPENED
    }

    override fun closeSearchBar() {
        _searchAppBarState.value = SearchAppBarState.CLOSED
    }

    override fun updateSearchText(value: String) {
        searchJob?.cancel()
        _searchText.value = value
        searchJob = scope.launch(Dispatchers.IO) {
            genreRepo.searchSeries(value.trim())
        }
    }

    @Composable
    override fun render() {
        GenreScreen(this)
    }
}