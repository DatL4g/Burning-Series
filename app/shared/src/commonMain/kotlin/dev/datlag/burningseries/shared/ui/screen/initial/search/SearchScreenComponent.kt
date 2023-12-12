package dev.datlag.burningseries.shared.ui.screen.initial.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.safeSubList
import dev.datlag.burningseries.model.state.SearchAction
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.network.state.SearchStateMachine
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.series.SeriesScreenComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class SearchScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit,
    private val scrollEnabled: (Boolean) -> Unit
) : SearchComponent, ComponentContext by componentContext {

    private val searchStateMachine: SearchStateMachine by di.instance()
    override val searchState: StateFlow<SearchState> = searchStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), SearchState.Loading)

    private val allGenres = searchState.mapNotNull { it as SearchState.Success }.map { it.genres }.stateIn(ioScope(), SharingStarted.WhileSubscribed(), emptyList())
    private val allItems = allGenres.map { it.flatMap { g -> g.items } }
    private val maxGenres = allGenres.map { it.size }
    private val loadedGenres = MutableStateFlow(1)

    override val genres: StateFlow<List<Genre>> = combine(allGenres, loadedGenres) { t1, t2 ->
        t1.safeSubList(0, t2)
    }.stateIn(ioScope(), SharingStarted.WhileSubscribed(), emptyList())

    override val canLoadMoreGenres: StateFlow<Boolean> = combine(loadedGenres, maxGenres) { t1, t2 ->
        t1 < t2
    }.stateIn(ioScope(), SharingStarted.WhileSubscribed(), allGenres.value.size > loadedGenres.value)


    private val searchQuery: MutableStateFlow<String> = MutableStateFlow(String())
    override val searchItems: StateFlow<List<Genre.Item>> = combine(allItems, searchQuery) { t1, t2 ->
        if (t2.isBlank()) {
            emptyList()
        } else {
            coroutineScope {
                t1.map {
                    async {
                        when {
                            it.title.trim().equals(t2.trim(), true) -> it to 1.0
                            it.title.trim().startsWith(t2.trim(), true) -> it to 0.95
                            it.title.trim().contains(t2.trim(), true) -> it to 0.9
                            else -> it to JaroWinkler.distance(it.title.trim(), t2.trim())
                        }
                    }
                }.awaitAll().filter {
                    it.second > 0.85
                }.sortedByDescending { it.second }.map { it.first }.safeSubList(0, 10)
            }
        }
    }.stateIn(ioScope(), SharingStarted.WhileSubscribed(), emptyList())

    private val navigation = SlotNavigation<SearchConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        serializer = SearchConfig.serializer(),
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
                watchVideo = { schemeKey, series, episode, stream ->
                    watchVideo(schemeKey, series, episode, stream)
                }
            )
        }
    }

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            SearchScreen(this)
        }
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