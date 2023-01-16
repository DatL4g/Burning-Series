package dev.datlag.burningseries.ui.screen.video

import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.value.MutableValue
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface VideoComponent : Component {

    val series: Series
    val episode: StateFlow<Series.Episode>
    val videoStreams: StateFlow<List<VideoStream>>
    val onGoBack: () -> Unit

    var playListener: (() -> Unit)?
    var playPauseListener: (() -> Unit)?
    var forwardListener: (() -> Unit)?
    var rewindListener: (() -> Unit)?
    var seekListener: ((Long) -> Unit)?

    val playIcon: MutableValue<ImageVector>
    val position: MutableValue<Long>
    val length: MutableValue<Long>

    val initialPosition: Flow<Long>

    fun triggerPlayPause()
    fun forward()
    fun rewind()

    fun seekTo(time: Long)

    fun playNextEpisode()
}