package dev.datlag.burningseries.ui.screen.initial.favorite

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.safeSubList
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.initial.search.SearchConfig
import dev.datlag.burningseries.ui.screen.initial.series.SeriesScreenComponent
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class FavoriteScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val watchVideo: (Collection<Stream>) -> Unit
) : FavoriteComponent, ComponentContext by componentContext {

    private val database: BurningSeries by di.instance()
    override val favorites = database
        .burningSeriesQueries
        .favoriteSeries()
        .asFlow()
        .mapToList(ioDispatcher())
        .stateIn(ioScope(), SharingStarted.Lazily, emptyList())

    private val searchQuery: MutableStateFlow<String> = MutableStateFlow(String())
    override val searchItems: StateFlow<List<Series>> = combine(favorites, searchQuery) { t1, t2 ->
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

    private val navigation = SlotNavigation<FavoriteConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        handleBackButton = false
    ) { config, context ->
        when (config) {
            is FavoriteConfig.Series -> SeriesScreenComponent(
                componentContext = context,
                di = di,
                initialTitle = config.title,
                initialHref = config.href,
                initialCoverHref = config.coverHref,
                onGoBack = navigation::dismiss,
                watchVideo = { watchVideo(it) }
            )
        }
    }

    @Composable
    override fun render() {
        FavoriteScreen(this)
    }

    override fun itemClicked(config: FavoriteConfig) {
        navigation.activate(config)
    }

    override fun searchQuery(text: String) {
        searchQuery.value = text.trim()
    }
}