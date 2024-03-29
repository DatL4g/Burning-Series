package dev.datlag.burningseries.shared.ui.screen.initial.favorite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.safeSubList
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.other.Crashlytics
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.series.SeriesScreenComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance
import dev.datlag.burningseries.model.Series as ModelSeries

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
        .flowOn(ioDispatcher())
        .stateIn(ioScope(), SharingStarted.WhileSubscribed(), emptyList())

    private val searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    override val searchItems: StateFlow<List<Series>> = combine(favorites, searchQuery) { t1, t2 ->
        if (t2.isBlank()) {
            t1
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
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), emptyList())

    private val navigation = SlotNavigation<FavoriteConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        serializer = FavoriteConfig.serializer(),
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
                    dismissHoldingSeries()
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
            FavoriteScreen(this)
        }
        SideEffect {
            Crashlytics.screen(this)
        }
    }

    override fun itemClicked(config: FavoriteConfig) {
        navigation.activate(config) {
            scrollEnabled(false)
        }
    }

    override fun searchQuery(text: String) {
        ioScope().launchIO {
            searchQuery.emit(text)
        }
    }

    override fun dismissHoldingSeries() {
        navigation.dismiss(scrollEnabled)
    }
}