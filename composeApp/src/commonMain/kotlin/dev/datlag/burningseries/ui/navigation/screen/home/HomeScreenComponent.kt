package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.ExtendedSeries
import dev.datlag.burningseries.database.common.favoritesSeries
import dev.datlag.burningseries.database.common.favoritesSeriesOneShot
import dev.datlag.burningseries.database.common.seriesFullHref
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.HomeStateMachine
import dev.datlag.burningseries.network.SearchStateMachine
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.network.state.SearchAction
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.burningseries.other.UserHelper
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.about.AboutDialogComponent
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.qrcode.QrCodeDialogComponent
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.release.ReleaseDialogComponent
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.SettingsDialogComponent
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync.SyncDialogComponent
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.decompose.ioScope
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val syncId: String?,
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

    private val userHelper by instance<UserHelper>()
    override val release: Flow<UserAndRelease.Release?> = userHelper.release
    override val displayRelease: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        initialConfiguration = { syncId?.ifBlank { null }?.let(DialogConfig::Sync) }
    ) { config, context ->
        when (config) {
            is DialogConfig.Settings -> SettingsDialogComponent(
                componentContext = context,
                di = di,
                onDismiss = dialogNavigation::dismiss,
                onAbout = {
                    dialogNavigation.activate(DialogConfig.About)
                }
            )
            is DialogConfig.Release -> ReleaseDialogComponent(
                componentContext = context,
                di = di,
                release = config.release,
                onDismiss = {
                    dialogNavigation.dismiss {
                        displayRelease.update { false }
                    }
                }
            )
            is DialogConfig.QrCode -> QrCodeDialogComponent(
                componentContext = context,
                di = di,
                onDismiss = dialogNavigation::dismiss
            )
            is DialogConfig.Sync -> SyncDialogComponent(
                componentContext = context,
                di = di,
                connectId = config.id,
                onDismiss = dialogNavigation::dismiss
            )
            is DialogConfig.About -> AboutDialogComponent(
                componentContext = context,
                di = di,
                onDismiss = dialogNavigation::dismiss
            )
        }
    }

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
        val savedSeries = database.seriesFullHref(data)?.let { href ->
            data.shadowCopy(href = href)
        }
        if (savedSeries != null) {
            onMedium(savedSeries, null)
        } else {
            onMedium(data, language)
        }
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

    override fun settings() {
        dialogNavigation.activate(DialogConfig.Settings)
    }

    override fun release(release: UserAndRelease.Release) {
        dialogNavigation.activate(DialogConfig.Release(release))
    }

    override fun showQrCode() {
        dialogNavigation.activate(DialogConfig.QrCode)
    }
}