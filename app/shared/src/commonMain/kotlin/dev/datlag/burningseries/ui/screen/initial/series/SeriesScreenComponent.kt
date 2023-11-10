package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackHandler
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.state.SeriesStateMachine
import io.ktor.client.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance

class SeriesScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val initialTitle: String,
    override val initialHref: String,
    override val initialCoverHref: String?,
    private val onGoBack: () -> Unit
) : SeriesComponent, ComponentContext by componentContext {

    private val httpClient by di.instance<HttpClient>()
    private val seriesStateMachine = SeriesStateMachine(httpClient, initialHref)
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, SeriesState.Loading(initialHref))

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