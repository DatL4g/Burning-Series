package dev.datlag.burningseries.ui.screen.initial.search

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.safeSubList
import dev.datlag.burningseries.model.state.SearchAction
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.network.state.SearchStateMachine
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.initial.series.SeriesScreenComponent
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class SearchScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val watchVideo: (Collection<Stream>) -> Unit,
    private val scrollEnabled: (Boolean) -> Unit
) : SearchComponent, ComponentContext by componentContext {

    private val searchStateMachine: SearchStateMachine by di.instance()
    override val searchState: StateFlow<SearchState> = searchStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, SearchState.Loading)

    private val allGenres = searchState.mapNotNull { it as SearchState.Success }.map { it.genres }.stateIn(ioScope(), SharingStarted.Lazily, emptyList())
    private val allItems = allGenres.map { it.flatMap { g -> g.items } }
    private val maxGenres = allGenres.map { it.size }
    private val loadedGenres = MutableStateFlow(1)

    override val genres: StateFlow<List<Genre>> = combine(allGenres, loadedGenres) { t1, t2 ->
        t1.safeSubList(0, t2)
    }.stateIn(ioScope(), SharingStarted.Lazily, emptyList())

    override val canLoadMoreGenres: StateFlow<Boolean> = combine(loadedGenres, maxGenres) { t1, t2 ->
        t1 < t2
    }.stateIn(ioScope(), SharingStarted.Lazily, allGenres.value.size > loadedGenres.value)


    private val searchQuery: MutableStateFlow<String> = MutableStateFlow(String())
    override val searchItems: StateFlow<List<Genre.Item>> = combine(allItems, searchQuery) { t1, t2 ->
        if (t2.isBlank()) {
            emptyList()
        } else {
            t1.map {
                when {
                    it.title.equals(t2, true) -> it to 1.0
                    it.title.startsWith(t2, true) -> it to 0.95
                    it.title.contains(t2, true) -> it to 0.9
                    else -> it to JaroWinkler.distance(it.title, t2)
                }
            }.filter {
                it.second > 0.85
            }.sortedByDescending { it.second }.map { it.first }.safeSubList(0, 10)
        }
    }.stateIn(ioScope(), SharingStarted.Lazily, emptyList())

    private val navigation = SlotNavigation<SearchConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        handleBackButton = false
    ) { config, context ->
        when (config) {
            is SearchConfig.Series -> SeriesScreenComponent(
                componentContext = context,
                di = di,
                initialTitle = config.title,
                initialHref = config.href,
                initialCoverHref = null,
                onGoBack = {
                    navigation.dismiss(scrollEnabled)
                },
                watchVideo = { watchVideo(it) }
            )
        }
    }

    @Composable
    override fun render() {
        SearchScreen(this)
    }

    override fun retryLoadingSearch(): Any? = ioScope().launchIO {
        searchStateMachine.dispatch(SearchAction.Retry)
    }

    override fun loadMoreGenres(): Any? = ioScope().launchIO {
        loadedGenres.update { it + 1 }
    }

    override fun searchQuery(text: String) {
        searchQuery.value = text.trim()
    }

    override fun itemClicked(config: SearchConfig) {
        navigation.activate(config) {
            scrollEnabled(false)
        }
    }
}