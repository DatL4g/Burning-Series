package dev.datlag.burningseries.ui.screen.video

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.screen.video.dialog.subtitle.SubtitleDialogComponent
import dev.datlag.burningseries.ui.theme.SchemeTheme
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val schemeKey: String,
    override val series: Series,
    private val initialEpisode: Series.Episode,
    private val initialStreams: List<Stream>,
    private val onBack: () -> Unit
) : VideoComponent, ComponentContext by componentContext {

    override val streams: List<Stream> = initialStreams.sortedBy { it.headers.size }
    private val episodeStateMachine by di.instance<EpisodeStateMachine>()
    override val episode: StateFlow<Series.Episode> = episodeStateMachine.state.mapNotNull { it as? EpisodeState.EpisodeHolder }.map { it.episode }.stateIn(ioScope(), SharingStarted.WhileSubscribed(), initialEpisode)

    private val database by di.instance<BurningSeries>()
    private val dbEpisode = episode.transform {
        return@transform emitAll(database.burningSeriesQueries.selectEpisodeByHref(it.href).asFlow().mapToOneOrNull(
            currentCoroutineContext()
        ))
    }.stateIn(ioScope(), SharingStarted.Lazily, database.burningSeriesQueries.selectEpisodeByHref(episode.value.href).executeAsOneOrNull())

    override val selectedSubtitle = MutableStateFlow<VideoComponent.Subtitle?>(null)

    override val startingPos: StateFlow<Long> = dbEpisode.transform {
        return@transform emit(it?.progress ?: 0L)
    }.stateIn(ioScope(), SharingStarted.Lazily, dbEpisode.value?.progress ?: 0L)

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        source = dialogNavigation
    ) { config, slotContext ->
        when (config) {
            is DialogConfig.Subtitle -> SubtitleDialogComponent(
                componentContext = slotContext,
                di = di,
                initialChosen = selectedSubtitle.value,
                list = config.list,
                onDismiss = dialogNavigation::dismiss,
                onChosen = {
                    selectedSubtitle.value = it
                }
            )
        }
    }

    private val backPressCounter = MutableStateFlow(0)
    private val backCallback = BackCallback {
        if (backPressCounter.value >= 1) {
            back()
        } else {
            backPressCounter.update { it + 1 }
        }
    }

    init {
        backHandler.register(backCallback)

        ioScope().launchIO {
            backPressCounter.collect { count ->
                if (count > 0) {
                    delay(2000)
                    backPressCounter.emit(0)
                }
            }
        }
    }

    @Composable
    override fun render() {
        SchemeTheme(schemeKey) {
            VideoScreen(this)
        }
    }

    override fun back() {
        onBack()
    }

    override fun ended() {
        // ToDo("load next episode")
    }

    override fun lengthUpdate(millis: Long) {
        val currentEpisode = episode.value

        database.burningSeriesQueries.updateEpisodeLength(
            length = millis,
            href = currentEpisode.href,
            number = currentEpisode.number,
            title = currentEpisode.title,
            progress = 0L,
            seriesHref = BSUtil.commonSeriesHref(series.href)
        )
    }

    override fun progressUpdate(millis: Long) {
        val currentEpisode = episode.value

        database.burningSeriesQueries.updateEpisodeProgress(
            progress = millis,
            href = currentEpisode.href,
            number = currentEpisode.number,
            title = currentEpisode.title,
            length = 0L,
            seriesHref = BSUtil.commonSeriesHref(series.href)
        )
    }

    override fun selectSubtitle(subtitles: List<VideoComponent.Subtitle>) {
        dialogNavigation.activate(DialogConfig.Subtitle(subtitles))
    }
}