package dev.datlag.burningseries.shared.ui.screen.initial.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Shortcut
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.getDigitsOrNull
import dev.datlag.burningseries.model.common.safeCast
import dev.datlag.burningseries.model.common.safeSubList
import dev.datlag.burningseries.model.state.*
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.network.state.ReleaseStateMachine
import dev.datlag.burningseries.network.state.SearchStateMachine
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.other.Crashlytics
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.home.dialog.sekret.SekretDialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.SeriesScreenComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private var shortcutIntent: Shortcut.Intent,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit,
    private val scrollEnabled: (Boolean) -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeStateMachine: HomeStateMachine by di.instance()
    override val homeState: StateFlow<HomeState> = homeStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), HomeState.Loading)
    override val onDeviceReachable = homeState.map {
        it.safeCast<HomeState.Success>()?.onDeviceReachable ?: (it is HomeState.Loading)
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), true)

    private val searchStateMachine by di.instance<SearchStateMachine>()
    override val searchState = searchStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = SearchState.Loading
    )
    private val allSearchItems = searchState.mapNotNull { it.safeCast<SearchState.Success>() }.map { it.genres.flatMap { g -> g.items } }
    private val searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    override val searchItems: StateFlow<List<Genre.Item>> = combine(allSearchItems, searchQuery) { t1, t2 ->
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
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), emptyList())

    private val appVersion: String? by di.instanceOrNull("APP_VERSION")
    private val releaseStateMachine: ReleaseStateMachine by di.instance()
    override val release: StateFlow<Release?> = releaseStateMachine.state.map { state ->
        if (state is ReleaseState.Success) {
            if (!appVersion.isNullOrBlank()) {
                state.releases.filter {
                    (it.tagAsNumber?.toIntOrNull() ?: 0) > (appVersion?.getDigitsOrNull()?.toIntOrNull() ?: 0)
                }.maxByOrNull { it.publishedAtSeconds }
            } else {
                state.releases.maxByOrNull { it.publishedAtSeconds }
            }
        } else {
            null
        }
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), null)

    private val navigation = SlotNavigation<HomeConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        serializer = HomeConfig.serializer(),
        handleBackButton = false,
        initialConfiguration = {
            when (val current = shortcutIntent) {
                is Shortcut.Intent.Series -> HomeConfig.Series(null, current.href, null)
                else -> null
            }
        }
    ) { config, context ->
        when (config) {
            is HomeConfig.Series -> SeriesScreenComponent(
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

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        key = "DialogChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer()
    ) { config, slotContext ->
        when (config) {
            DialogConfig.Sekret -> SekretDialogComponent(
                componentContext = slotContext,
                di = di,
                onDismissed = dialogNavigation::dismiss
            )
        }
    }

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            HomeScreen(this)
        }
        SideEffect {
            Crashlytics.screen(this)
        }
    }

    override fun retryLoadingHome(): Any? = ioScope().launchIO {
        homeStateMachine.dispatch(HomeAction.Retry)
    }

    override fun itemClicked(config: HomeConfig) {
        navigation.activate(config) {
            scrollEnabled(false)
        }
    }

    override fun showDialog(config: DialogConfig) {
        dialogNavigation.activate(config)
    }

    override fun dismissHoldingSeries() {
        shortcutIntent = Shortcut.Intent.NONE
        navigation.dismiss { success ->
            scrollEnabled(success)

            if (!success) {
                searchQuery.getAndUpdate {
                    if (it.isBlank()) {
                        it
                    } else {
                        ""
                    }
                }
            }
        }
    }

    override fun searchQuery(text: String) {
        ioScope().launchIO {
            searchQuery.emit(text)
        }
    }

    override fun retryLoadingSearch() {
        ioScope().launchIO {
            searchStateMachine.dispatch(SearchAction.Retry)
        }
    }
}