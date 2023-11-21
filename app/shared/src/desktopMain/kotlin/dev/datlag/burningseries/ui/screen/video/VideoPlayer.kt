package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import org.apache.commons.lang3.SystemUtils
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.MediaPlayerComponent

@Composable
fun VideoPlayer(
    component: VideoComponent,
    modifier: Modifier
): dev.datlag.burningseries.ui.screen.video.MediaPlayer? {
    val foundVlc = NativeDiscovery().discover()

    if (foundVlc) {
        val mediaPlayer = remember {
            if (SystemUtils.IS_OS_MAC) {
                CallbackMediaPlayerComponent()
            } else {
                EmbeddedMediaPlayerComponent()
            }
        }

        Box(
            modifier = modifier
        ) {
            SwingPanel(
                background = Color.Black,
                modifier = Modifier.fillMaxSize(),
                factory = {
                    mediaPlayer
                }
            )
        }

        val streamList = remember { component.streams }

        var streamIndex by remember(streamList) { mutableIntStateOf(0) }
        var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
        val url = remember(streamIndex, sourceIndex) { streamList[streamIndex].list[sourceIndex] }
        val headers = remember(streamIndex) {
            streamList[streamIndex].headers
        }

        val isPlaying = remember { mutableStateOf(false) }
        val length = remember { mutableLongStateOf(0) }
        val time = remember { mutableLongStateOf(0) }

        val eventListener = remember { object : MediaPlayerEventAdapter() {
            override fun error(mediaPlayer: MediaPlayer?) {
                super.error(mediaPlayer)

                if (streamList[streamIndex].list.size - 1 > sourceIndex) {
                    sourceIndex++
                } else if (streamList.size - 1 > streamIndex) {
                    streamIndex++
                }
            }

            override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
                super.lengthChanged(mediaPlayer, newLength)

                component.lengthUpdate(newLength)
                length.value = newLength
            }

            override fun finished(mediaPlayer: MediaPlayer?) {
                super.finished(mediaPlayer)

                component.ended()
            }

            override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
                super.timeChanged(mediaPlayer, newTime)

                component.progressUpdate(newTime)
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
        } }

        LaunchedEffect(mediaPlayer, eventListener) {
            mediaPlayer.mediaPlayer()?.events()?.addMediaPlayerEventListener(eventListener)
        }

        SideEffect {
            applyHeaders(headers, mediaPlayer.mediaPlayer())
            mediaPlayer.mediaPlayer()?.media()?.play(url)
        }

        DisposableEffect(mediaPlayer) {
            onDispose {
                mediaPlayer.mediaPlayer()?.release()
            }
        }

        return remember(mediaPlayer) { object : dev.datlag.burningseries.ui.screen.video.MediaPlayer {
            override val isPlaying: MutableState<Boolean> = isPlaying
            override val length: MutableLongState = length
            override val time: MutableLongState = time

            override fun play() {
                mediaPlayer.mediaPlayer()?.controls()?.play()
            }

            override fun pause() {
                mediaPlayer.mediaPlayer()?.controls()?.pause()
            }

            override fun rewind() {
                mediaPlayer.mediaPlayer()?.controls()?.skipTime(-10000)
            }

            override fun forward() {
                mediaPlayer.mediaPlayer()?.controls()?.skipTime(10000)
            }

            override fun seekTo(millis: Long) {
                mediaPlayer.mediaPlayer()?.controls()?.setTime(millis)
            }
        } }
    }
    return null
}

private fun MediaPlayerComponent.mediaPlayer(): MediaPlayer? {
    return when (this) {
        is CallbackMediaPlayerComponent -> mediaPlayer()
        is EmbeddedMediaPlayerComponent -> mediaPlayer()
        else -> null
    }
}

private fun applyHeaders(headers: Map<String, String>, mediaPlayer: MediaPlayer?) {
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
        mediaPlayer?.media()?.options()?.add("--http-referrer", referer)
    }
}