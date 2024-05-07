package dev.datlag.burningseries.shared.ui.screen.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.comimport androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.checked
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.events.KeyboardEvent

fun main() {
    renderComposable(rootElementId = "app") {
        var fullscreenEnabled by remember { mutableStateOf(false) }

        Div {
            Input(
                type = InputType.Checkbox,
                attrs = {
                    checked(fullscreenEnabled)
                    onChange {
                        fullscreenEnabled = it.value == "on"
                    }
                }
            )

            Label(attrs = {
                style {
                    marginLeft(5.px)
                }
            }) {
                Text("Enable Fullscreen")
            }

            VideoPlayer(component = VideoComponent(), modifier = Modifier, fullscreenEnabled = fullscreenEnabled)
        }

        WindowEvents(keyboardEvents = {
            on { e ->
                if (e.type == KeyEventType.KeyDown && e.sourceEvent is KeyboardEvent) {
                    val keyEvent = e.sourceEvent as KeyboardEvent
                    if (keyEvent.key == Key.F11) {
                        if (fullscreenEnabled) {
                            // Call exitFullscreen method
                        } else {
                            // Call enterFullscreen method
                        }
                        fullscreenEnabled = !fullscreenEnabled
                        e.consume()
                    }
                }
            }
        })
    }
}

fun VideoPlayer(component: VideoComponent, modifier: Modifier, fullscreenEnabled: Boolean): VideoComponent? {
    // Existing VideoPlayer code with modifications for fullscreenEnabled parameter
    // Ensure to update the VideoPlayer function based on the existing code
    // Implement logic to handle entering and exiting fullscreen mode based on the fullscreenEnabled flag
    return component
}

fun enterFullscreen() {
    // Logic to enter fullscreen mode
}

fun exitFullscreen() {
    // Logic to exit fullscreen mode
}pose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import org.apache.commons.lang3.SystemUtils
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.MediaPlayerComponent
import kotlin.math.roundToInt

@Composable
fun VideoPlayer(
    component: VideoComponent,
    modifier: Modifier
): dev.datlag.burningseries.shared.ui.screen.video.MediaPlayer? {
    val foundVlc = NativeDiscovery().discover()

    if (foundVlc) {
        val mediaPlayerComponent = remember {
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
                    mediaPlayerComponent
                }
            )
        }

        val streamList = remember { component.streams }

        var streamIndex by remember(streamList) { mutableIntStateOf(0) }
        var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
        val url = remember(streamIndex, sourceIndex) { streamList[streamIndex].sources.toList()[sourceIndex] }
        val headers = remember(streamIndex) {
            streamList[streamIndex].headers
        }
        val startingPos by component.startingPos.collectAsStateWithLifecycle()

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

            override fun opening(mediaPlayer: MediaPlayer?) {
                super.opening(mediaPlayer)

                (mediaPlayer ?: mediaPlayerComponent.mediaPlayer())?.controls()?.setTime(startingPos)
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
            applyHeaders(headers, mediaPlayerComponent.mediaPlayer())
            mediaPlayerComponent.mediaPlayer()?.media()?.play(url)
        }

        DisposableEffect(mediaPlayerComponent) {
            onDispose {
                mediaPlayerComponent.mediaPlayer()?.release()
            }
        }

        return remember(mediaPlayerComponent) { object : dev.datlag.burningseries.shared.ui.screen.video.MediaPlayer {
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