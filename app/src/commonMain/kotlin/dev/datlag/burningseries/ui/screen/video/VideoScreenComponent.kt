package dev.datlag.burningseries.ui.screen.video

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.model.common.getDigitsOrNull
import dev.datlag.burningseries.network.repository.EpisodeRepository
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File
import kotlin.math.max
import dev.datlag.burningseries.common.CommonDispatcher
import kotlinx.coroutines.flow.*

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val series: Series,
    private val initialEpisode: Series.Episode,
    private val initialVideoStreams: List<VideoStream>,
    override val onGoBack: () -> Unit,
    override val di: DI
) : VideoComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    override var forwardListener: (() -> Unit)? = null
    override var playListener: (() -> Unit)? = null
    override var playPauseListener: (() -> Unit)? = null
    override var rewindListener: (() -> Unit)? = null
    override var seekListener: ((Long) -> Unit)? = null

    private val db: BurningSeriesDB by di.instance()
    private val imageDir: File by di.instance("ImageDir")
    private val episodeRepo: EpisodeRepository by di.instance()

    override val episode = MutableStateFlow(initialEpisode)
    override val videoStreams = MutableStateFlow(initialVideoStreams)

    private val dbEpisode = episode.transformLatest { episode ->
        return@transformLatest emitAll(db.burningSeriesQueries.selectEpisodeByHref(
            episode.href.trimHref()
        ).asFlow().mapToOneOrNull(Dispatchers.IO))
    }.flowOn(Dispatchers.IO)

    private val hosterList = db.burningSeriesQueries.selectAllHosters().asFlow().mapToList(Dispatchers.IO)

    override val initialPosition: Flow<Long> = dbEpisode.map { it?.watchProgress ?: 0 }
    private val initialLength: Flow<Long> = dbEpisode.map { it?.length ?: 0 }

    override val playIcon: MutableValue<ImageVector> = MutableValue(Icons.Default.MoreHoriz)
    override val position: MutableValue<Long> = MutableValue(initialPosition.getValueBlocking(0))
    override val length: MutableValue<Long> = MutableValue(initialLength.getValueBlocking(0))

    private var loadingNextEpisode = false
    private var nextEpisode: Pair<Series.Episode, List<VideoStream>>? = null
    private var loopEpisode: Series.Episode = episode.value

    private val episodeSaveFlow = episode.transformLatest { episode ->
        val seriesHref = series.href.buildTitleHref()
        val episodeHref = episode.href.trimHref()

        return@transformLatest emit(db.burningSeriesQueries.insertEpisodeOrIgnore(
            episodeHref,
            episode.title,
            max(initialLength.first(), if (episode.watched == true) Long.MAX_VALUE else 0L),
            max(initialPosition.first(), if (episode.watched == true) Long.MAX_VALUE else 0L),
            Clock.System.now().epochSeconds,
            episode.watchHref,
            seriesHref
        ))
    }.flowOn(Dispatchers.IO)

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
        val seriesHref = series.href.buildTitleHref()

        scope.launch(Dispatchers.IO) {
            val coverFile = File(imageDir, "${seriesHref.fileName()}.bs")

            if (!coverFile.exists()) {
                val coverBase64 = series.cover.base64

                if (coverBase64.isNotEmpty()) {
                    try {
                        coverFile.writeText(coverBase64)
                    } catch (ignored: Throwable) { }
                }
            }

            db.burningSeriesQueries.insertSeriesIfNotExists(
                seriesHref,
                series.href,
                series.title,
                series.cover.href,
                0L
            )
        }
        scope.launch(Dispatchers.IO) {
            episodeSaveFlow.collect()
        }
        scope.launch(Dispatchers.IO) {
            episode.collect {
                val episodeHref = it.href.trimHref()
                loopEpisode = it

                while (this.isActive && loopEpisode == it) {
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
                    if (pos > 0 && length > 0 && pos >= length / 2) {
                        loadNextEpisode(it)
                    }
                    if (length > 0) {
                        db.burningSeriesQueries.updateEpisodeLength(
                            length,
                            episodeHref
                        )
                    }

                    db.burningSeriesQueries.updateEpisodeLastWatched(
                        Clock.System.now().epochSeconds,
                        episodeHref
                    )

                    delay(3000)
                }
            }
        }
    }

    private suspend fun loadNextEpisode(currentEpisode: Series.Episode) {
        if (!loadingNextEpisode) {
            loadingNextEpisode = true

            fun fallbackNextEpisode(): Series.Episode? {
                val sortedEpisodes = series.episodes.sortedBy { it.number.toIntOrNull() }
                val currentIndex = sortedEpisodes.indexOf(currentEpisode)
                return if (currentIndex > -1 && currentIndex < sortedEpisodes.size - 1) {
                    sortedEpisodes[currentIndex + 1]
                } else {
                    null
                }
            }

            val currentEpisodeNumber = currentEpisode.number.toIntOrNull() ?: currentEpisode.number.getDigitsOrNull()?.toIntOrNull() ?: currentEpisode.episodeNumberOrListNumber

            val nextEpisode = if (currentEpisodeNumber != null) {
                series.episodes.find {
                    val nextEpisodeNumber = it.number.toIntOrNull() ?: it.number.getDigitsOrNull()?.toIntOrNull() ?: it.episodeNumberOrListNumber
                    nextEpisodeNumber == currentEpisodeNumber + 1
                }
            } else {
                null
            } ?: fallbackNextEpisode()

            if (nextEpisode != null) {
                episodeRepo.loadHosterStreams(nextEpisode)
                val episodeData = episodeRepo.streams.first()
                val hosterList = hosterList.first()

                if (episodeData.isNotEmpty()) {
                    val sortedList = episodeData.sortedBy { stream ->
                        hosterList.find { it.name.equals(stream.hoster.hoster, true) }?.position ?: Int.MAX_VALUE
                    }
                    this.nextEpisode = nextEpisode to sortedList
                }
            } else {
                this.nextEpisode = null
            }
        }
    }

    override fun playNextEpisode() {
        scope.launch(Dispatchers.IO) {
            nextEpisode?.let { (episode, streams) ->
                videoStreams.emit(streams)
                this@VideoScreenComponent.episode.emit(episode)
                loadingNextEpisode = false

                delay(100)

                withContext(CommonDispatcher.Main) {
                    playListener?.invoke()
                }
            }
        }
    }

    @Composable
    override fun render() {
        VideoScreen(this)
    }
}