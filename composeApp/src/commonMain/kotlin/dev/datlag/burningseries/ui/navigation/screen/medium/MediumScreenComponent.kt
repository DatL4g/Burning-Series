package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.SeriesStateMachine
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.decompose.ioScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance

class MediumScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val initialSeriesData: SeriesData
) : MediumComponent, ComponentContext by componentContext {

    private val seriesStateMachine by instance<SeriesStateMachine>()
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = seriesStateMachine.currentState
    )

    init {
        seriesStateMachine.href(initialSeriesData.toHref())
    }

    @Composable
    override fun render() {
        val haze = remember { HazeState() }

        CompositionLocalProvider(
            LocalHaze provides haze
        ) {
            onRender {
                MediumScreen(this)
            }
        }
    }
}