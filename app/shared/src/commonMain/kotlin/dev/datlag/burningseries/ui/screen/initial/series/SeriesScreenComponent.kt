package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import dev.datlag.burningseries.common.defaultScope
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.state.SeriesStateMachine
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.screen.initial.series.dialog.season.SeasonDialogComponent
import io.ktor.client.*
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class SeriesScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val initialTitle: String,
    private val initialHref: String,
    private val initialCoverHref: String?,
    private val onGoBack: () -> Unit
) : SeriesComponent, ComponentContext by componentContext {

    private val httpClient by di.instance<HttpClient>()
    private val seriesStateMachine = SeriesStateMachine(httpClient, initialHref)
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, SeriesState.Loading(initialHref))

    private val currentSeries = seriesState.mapNotNull { it as? SeriesState.Success }.map { it.series }.stateIn(ioScope(), SharingStarted.Lazily, null)
    override val title: StateFlow<String> = currentSeries.mapNotNull { it?.title }.stateIn(ioScope(), SharingStarted.Lazily, initialTitle)
    override val href: StateFlow<String> = currentSeries.mapNotNull { it?.href }.stateIn(ioScope(), SharingStarted.Lazily, BSUtil.fixSeriesHref(initialHref))
    override val coverHref: StateFlow<String?> = currentSeries.mapNotNull { it?.coverHref }.stateIn(ioScope(), SharingStarted.Lazily, initialCoverHref)

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    private val _dialog = childSlot(
        source = dialogNavigation
    ) { config, slotContext ->
        when (config) {
            is DialogConfig.Season -> SeasonDialogComponent(
                componentContext = slotContext,
                di = di,
                defaultSeason = config.selected,
                seasons = config.seasons,
                onDismissed = dialogNavigation::dismiss,
                onSelected = {
                    loadNewSeason(it)
                }
            )
        }
    }
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = _dialog

    private val backCallback = BackCallback(priority = Int.MAX_VALUE) {
        onGoBack()
    }

    init {
        backHandler.register(backCallback)
    }

    @Composable
    override fun render() {
        SeriesScreen(this)
    }

    override fun retryLoadingSeries(): Any? = ioScope().launchIO {
        seriesStateMachine.dispatch(SeriesAction.Retry)
    }

    override fun goBack() {
        onGoBack()
    }

    override fun showDialog(config: DialogConfig) {
        dialogNavigation.activate(config)
    }

    private fun loadNewSeason(season: Series.Season) = ioScope().launchIO {
        (currentSeries.value ?: currentSeries.firstOrNull())?.let { series ->
            seriesStateMachine.dispatch(SeriesAction.Load(series.hrefBuilder(season.value)))
        }
    }
}