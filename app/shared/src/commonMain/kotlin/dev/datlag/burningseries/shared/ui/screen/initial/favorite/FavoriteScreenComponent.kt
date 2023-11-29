package dev.datlag.burningseries.shared.ui.screen.initial.favorite

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.model.Series as ModelSeries
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.safeSubList
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.series.SeriesScreenComponent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class FavoriteScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val watchVideo: (String, ModelSeries, ModelSeries.Episode, Collection<Stream>) -> Unit,
    private val scrollEnabled: (Boolean) -> Unit
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
        FavoriteScreen(this)
    }

    override fun itemClicked(config: FavoriteConfig) {
        navigation.activate(config) {
            scrollEnabled(false)
        }
    }

    override fun searchQuery(text: String) {
        searchQuery.value = text.trim()
    }
}