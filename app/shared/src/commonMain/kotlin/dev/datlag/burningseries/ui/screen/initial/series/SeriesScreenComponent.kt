package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.state.SeriesStateMachine
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

    override val title: StateFlow<String> = seriesState.mapNotNull { it as? SeriesState.Success }.map { it.series.title }.stateIn(ioScope(), SharingStarted.Lazily, initialTitle)
    override val href: StateFlow<String> = seriesState.mapNotNull { it as? SeriesState.Success }.map { it.series.href }.stateIn(ioScope(), SharingStarted.Lazily, BSUtil.fixSeriesHref(initialHref))
    override val coverHref: StateFlow<String?> = seriesState.mapNotNull { it as? SeriesState.Success }.mapNotNull { it.series.coverHref }.stateIn(ioScope(), SharingStarted.Lazily, initialCoverHref)

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
}