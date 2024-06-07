package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.SeriesStateMachine
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.decompose.ioScope
import dev.datlag.tooling.safeCast
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance

class MediumScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val initialSeriesData: SeriesData,
    override val initialIsAnime: Boolean,
    private val onBack: () -> Unit
) : MediumComponent, ComponentContext by componentContext {

    private val seriesStateMachine by instance<SeriesStateMachine>()
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = seriesStateMachine.currentState
    )

    private val successState = seriesState.mapNotNull {
        it.safeCast<SeriesState.Success>()
    }

    override val seriesTitle: Flow<String> = successState.map { it.series.mainTitle }
    override val seriesSubTitle: Flow<String?> = successState.map { it.series.subTitle }
    override val seriesCover: Flow<String?> = successState.map { it.series.coverHref ?: initialSeriesData.coverHref }
    override val seriesInfo: Flow<ImmutableCollection<Series.Info>> = successState.map { it.series.infoWithoutGenre }
    override val seriesDescription: Flow<String> = successState.map { it.series.description }
    override val seriesIsAnime: Flow<Boolean> = successState.map { it.series.isAnime }
    override val episodes: Flow<ImmutableCollection<Series.Episode>> = successState.map { it.series.episodes }

    init {
        seriesStateMachine.href(initialSeriesData.toHref())
    }

    @Composable
    override fun render() {
        val haze = remember { HazeState() }

        CompositionLocalProvider(
            LocalHaze provides haze
        ) {
            onRenderWithScheme(initialSeriesData.source) {
                MediumScreen(this, it)
            }
        }
    }

    override fun back() {
        onBack()
    }
}