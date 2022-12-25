package dev.datlag.burningseries.ui.screen.video

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.buildTitleHref
import dev.datlag.burningseries.common.trimHref
import dev.datlag.burningseries.database.BurningSeriesDB
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.instance

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val series: Series,
    override val episode: Series.Episode,
    override val videoStreams: List<VideoStream>,
    override val onGoBack: () -> Unit,
    override val di: DI
) : VideoComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    override var forwardListener: (() -> Unit)? = null
    override var playPauseListener: (() -> Unit)? = null
    override var rewindListener: (() -> Unit)? = null
    override var seekListener: ((Long) -> Unit)? = null

    private val db: BurningSeriesDB by di.instance()
    private val dbEpisode = db.burningSeriesQueries.selectEpisodeByHref(
        episode.href.trimHref()
    ).executeAsOneOrNull()
    override val initialPosition: Long = dbEpisode?.watchProgress ?: 0
    private val initialLength: Long = dbEpisode?.length ?: 0

    override val playIcon: MutableValue<ImageVector> = MutableValue(Icons.Default.MoreHoriz)
    override val position: MutableValue<Long> = MutableValue(initialPosition)
    override val length: MutableValue<Long> = MutableValue(initialLength)

    override fun forward() {
        forwardListener?.invoke()
    }

    override fun triggerPlayPause() {
        playPauseListener?.invoke()
    }

    override fun rewind() {
        rewindListener?.invoke()
    }

    override fun seekTo(time: Long) {
        seekListener?.invoke(time)
    }

    init {
        scope.launch(Dispatchers.IO) {
            val episodeHref = episode.href.trimHref()
            val seriesHref = series.href.buildTitleHref()
            var addedLength = false

            db.burningSeriesQueries.insertEpisode(
                episodeHref,
                episode.title,
                episode.number,
                initialLength,
                initialPosition,
                seriesHref
            )

            while (this.isActive) {
                val pos = withContext(CommonDispatcher.Main) {
                    position.value
                }
                val length = withContext(CommonDispatcher.Main) {
                    length.value
                }

                if (pos >= 3000) {
                    db.burningSeriesQueries.updateEpisodeWatchProgress(
                        pos,
                        episodeHref
                    )
                }
                if (length > 0 && !addedLength) {
                    db.burningSeriesQueries.updateEpisodeLength(
                        length,
                        episodeHref
                    )
                    addedLength = true
                }
                delay(3000)
            }
        }
    }

    @Composable
    override fun render() {
        VideoScreen(this)
    }
}