package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import dev.datlag.tooling.Platform
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.MediaPlayerComponent
import java.awt.Component
import kotlin.math.roundToInt

@Composable
internal fun VideoPlayer(
    component: VideoComponent
): dev.datlag.burningseries.ui.navigation.screen.video.MediaPlayer {
    val mediaPlayerComponent = remember {
        if (Platform.isMacOS) {
            CallbackMediaPlayerComponent()
        } else {
            EmbeddedMediaPlayerComponent()
        }
    }

    val streamList = remember { component.streams.toImmutableList() }

    var streamIndex by remember(streamList) { mutableIntStateOf(0) }
    var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
    val url = remember(streamIndex, sourceIndex) { streamList[streamIndex].sources.toImmutableList()[sourceIndex] }
    val headers = remember(streamIndex) {
        streamList[streamIndex].headers
    }

    val isPlaying = remember { mutableStateOf(false) }
    val length = remember { mutableLongStateOf(0) }
    val time = remember { mutableLongStateOf(0) }
    val isMuted = remember { mutableStateOf(mediaPlayerComponent.mediaPlayer()?.audio()?.isMute ?: false) }
    val volumeState = remember {
        val current = mediaPlayerComponent.mediaPlayer()?.audio()?.volume()?.toFloat() ?: 0F
        val set = if (current <= 0F) {
            if (isMuted.value) {
                0F
            } else {
                1F
            }
        } else {
            current
        }

        mutableFloatStateOf(set)
    }

    val eventListener = remember { object : MediaPlayerEventAdapter() {
        override fun error(mediaPlayer: MediaPlayer?) {
            super.error(mediaPlayer)

            if (streamList[streamIndex].sources.size - 1 > sourceIndex) {
                sourceIndex++
            } else if (streamList.size - 1 > streamIndex) {
                streamIndex++
            }
        }

        override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
            super.lengthChanged(mediaPlayer, newLength)

            component.length(newLength)
            length.value = newLength
        }

        override fun finished(mediaPlayer: MediaPlayer?) {
            super.finished(mediaPlayer)

            component.ended()
        }

        override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
            super.timeChanged(mediaPlayer, newTime)

            component.progress(newTime)
            time.value = newTime
        }

        override fun playing(mediaPlayer: MediaPlayer?) {
            super.playing(mediaPlayer)

            isPlaying.value = true
        }

        override fun paused(mediaPlayer: MediaPlayer?) {
            super.paused(mediaPlayer)

            isPlaying.value = false
        }

        override fun opening(mediaPlayer: MediaPlayer?) {
            super.opening(mediaPlayer)

            (mediaPlayer ?: mediaPlayerComponent.mediaPlayer())?.controls()?.setTime(component.startingPos)
        }

        override fun muted(mediaPlayer: MediaPlayer?, muted: Boolean) {
            super.muted(mediaPlayer, muted)

            isMuted.value = muted
        }

        override fun volumeChanged(mediaPlayer: MediaPlayer?, volume: Float) {
            super.volumeChanged(mediaPlayer, volume)

            volumeState.value = volume * 100
        }
    } }

    LaunchedEffect(mediaPlayerComponent, eventListener) {
        mediaPlayerComponent.mediaPlayer()?.events()?.addMediaPlayerEventListener(eventListener)
    }

    SideEffect {
        mediaPlayerComponent.mediaPlayer().applyHeaders(headers)
        mediaPlayerComponent.mediaPlayer()?.media()?.prepare(url)
    }

    DisposableEffect(mediaPlayerComponent) {
        onDispose {
            mediaPlayerComponent.mediaPlayer()?.release()
        }
    }

    return remember(mediaPlayerComponent) { object : dev.datlag.burningseries.ui.navigation.screen.video.MediaPlayer {
        override val component: Component = mediaPlayerComponent
        override val isPlaying: MutableState<Boolean> = isPlaying
        override val length: MutableLongState = length
        override val time: MutableLongState = time
        override val isMuted: MutableState<Boolean> = isMuted
        override val volume: MutableFloatState = volumeState

        override fun play() {
            mediaPlayerComponent.mediaPlayer()?.controls()?.play()
        }

        override fun pause() {
            mediaPlayerComponent.mediaPlayer()?.controls()?.pause()
        }

        override fun rewind() {
            mediaPlayerComponent.mediaPlayer()?.controls()?.skipTime(-10000)
        }

        override fun forward() {
            mediaPlayerComponent.mediaPlayer()?.controls()?.skipTime(10000)
        }

        override fun seekTo(millis: Long) {
            mediaPlayerComponent.mediaPlayer()?.controls()?.setTime(millis)
        }

        override fun mute() {
            mediaPlayerComponent.mediaPlayer()?.audio()?.isMute = true
        }

        override fun unmute() {
            mediaPlayerComponent.mediaPlayer()?.audio()?.isMute = false
        }

        override fun setVolume(volume: Float) {
            mediaPlayerComponent.mediaPlayer()?.audio()?.setVolume(volume.roundToInt())
        }

        override fun startPlaying() {
            mediaPlayerComponent.mediaPlayer()?.media()?.play(url)
        }
    } }
}

private fun MediaPlayerComponent.mediaPlayer(): MediaPlayer? {
    return when (this) {
        is CallbackMediaPlayerComponent -> mediaPlayer()
        is EmbeddedMediaPlayerComponent -> mediaPlayer()
        else -> null
    }
}

private fun MediaPlayer?.applyHeaders(headers: Map<String, String>) {
    if (headers.containsKey("Referer")) {
        val referer = headers.getOrElse("Referer") {
            headers.entries.firstNotNullOf {
                if (it.key.equals("Referer", true)) {
                    it.value
                } else {
                    null
                }
            }
        }
        this?.media()?.options()?.add("--http-referrer", referer)
    }
}
