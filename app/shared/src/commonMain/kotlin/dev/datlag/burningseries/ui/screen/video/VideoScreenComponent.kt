package dev.datlag.burningseries.ui.screen.video

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.ui.theme.SchemeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
    override val episode: StateFlow<Series.Episode> = MutableStateFlow(initialEpisode)

    private val database by di.instance<BurningSeries>()

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
}