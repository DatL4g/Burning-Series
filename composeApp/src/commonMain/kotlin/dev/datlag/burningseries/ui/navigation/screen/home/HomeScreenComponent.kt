package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.ExtendedSeries
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.database.common.favoritesSeries
import dev.datlag.burningseries.database.common.favoritesSeriesOneShot
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.HomeStateMachine
import dev.datlag.burningseries.network.SearchStateMachine
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.network.state.SearchAction
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.decompose.ioScope
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onMedium: (SeriesData, Language?) -> Unit
): HomeComponent, ComponentContext by componentContext {

    private val homeStateMachine by instance<HomeStateMachine>()
    override val home: StateFlow<HomeState> = homeStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = homeStateMachine.currentState
    )

    private val searchStateMachine by instance<SearchStateMachine>()
    override val search: StateFlow<SearchState> = searchStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = searchStateMachine.currentState
    )

    private val database by instance<BurningSeries>()
    override val showFavorites: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val favorites: StateFlow<ImmutableCollection<ExtendedSeries>> = database.favoritesSeries(
        ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = database.favoritesSeriesOneShot()
    )

    private val settings by instance<Settings.PlatformAppSettings>()
    override val language = settings.language.flowOn(ioDispatcher())

    @Composable
    override fun render() {
        val haze = remember { HazeState() }

        CompositionLocalProvider(
            LocalHaze provides haze
        ) {
            onRender {
                HomeScreen(this)
            }
        }
    }

    override fun details(data: SeriesData, language: Language?) {
        onMedium(data, language)
    }

    override fun search(query: String?) {
        launchIO {
            searchStateMachine.dispatch(SearchAction.Query(query?.ifBlank { null }))
        }
    }

    override fun retryLoadingSearch() {
        launchIO {
            searchStateMachine.dispatch(SearchAction.Retry)
        }
    }

    override fun toggleFavorites() {
        showFavorites.update { !it }
    }
}