package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.common.episodeProgress
import dev.datlag.burningseries.database.common.insertEpisodeOrIgnore
import dev.datlag.burningseries.database.common.updateLength
import dev.datlag.burningseries.database.common.updateProgress
import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.Stream
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.decompose.ioScope
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.math.max

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val series: Series,
    override val episode: Series.Episode,
    override val streams: ImmutableCollection<Stream>,
    private val onBack: () -> Unit
) : VideoComponent, ComponentContext by componentContext {

    private val database by instance<BurningSeries>()
    override val startingPos: Long = max(database.episodeProgress(episode).executeAsOneOrNull() ?: 0L, 0L)

    init {
        database.insertEpisodeOrIgnore(
            episode = episode,
            series = series,
        )
    }

    @Composable
    override fun render() {
        onRender {
            VideoScreen(this)
        }
    }

    override fun back() {
        onBack()
    }

    override fun ended() {
        // ToDo("load next")
    }

    override fun length(value: Long) {
        database.updateLength(value, episode)
    }

    override fun progress(value: Long) {
        database.updateProgress(value, episode)
    }
}