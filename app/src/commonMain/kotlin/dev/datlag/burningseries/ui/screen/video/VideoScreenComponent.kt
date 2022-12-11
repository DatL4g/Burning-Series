package dev.datlag.burningseries.ui.screen.video

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val series: Series,
    override val episode: Series.Episode,
    override val videoStreams: List<VideoStream>,
    override val onGoBack: () -> Unit,
    override val di: DI
) : VideoComponent, ComponentContext by componentContext {

    override var forwardListener: (() -> Unit)? = null
    override var playPauseListener: (() -> Unit)? = null
    override var rewindListener: (() -> Unit)? = null
    override var seekListener: ((Long) -> Unit)? = null

    override val playIcon: MutableValue<ImageVector> = MutableValue(Icons.Default.MoreHoriz)
    override val position: MutableValue<Long> = MutableValue(0)
    override val length: MutableValue<Long> = MutableValue(0)

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

    @Composable
    override fun render() {
        VideoScreen(this)
    }
}