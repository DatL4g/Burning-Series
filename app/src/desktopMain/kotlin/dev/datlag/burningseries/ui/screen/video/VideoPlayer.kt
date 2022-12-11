package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.MediaPlayerComponent
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VideoPlayer(
    component: VideoComponent
) {
    val foundVlc = NativeDiscovery().discover()
    val scope = rememberCoroutineScope()

    if (foundVlc) {
        val mediaPlayerComponent = remember {
            if (isMacOs()) {
                CallbackMediaPlayerComponent()
            } else {
                EmbeddedMediaPlayerComponent()
            }
        }
        var streamListPos by remember { mutableStateOf(0) }
        var srcListPos by remember { mutableStateOf(0) }

        SideEffect {
            mediaPlayerComponent.mediaPlayer()?.media()?.play(component.videoStreams[streamListPos].srcList[srcListPos])
            mediaPlayerComponent.mediaPlayer()?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                override fun error(mediaPlayer: MediaPlayer?) {
                    super.error(mediaPlayer)
                    var play = false
                    if (component.videoStreams[streamListPos].srcList.size - 1 > srcListPos) {
                        srcListPos++
                        play = true
                    } else {
                        if (component.videoStreams.size - 1 > streamListPos) {
                            streamListPos++
                            srcListPos = 0
                            play = true
                        }
                    }

                    if (play) scope.launch(Dispatchers.Main) {
                        mediaPlayer?.media()?.play(component.videoStreams[streamListPos].srcList[srcListPos])
                    }
                }

                override fun playing(mediaPlayer: MediaPlayer?) {
                    super.playing(mediaPlayer)
                    scope.launch(Dispatchers.Main) {
                        component.playIcon.value = Icons.Default.Pause
                    }
                }

                override fun paused(mediaPlayer: MediaPlayer?) {
                    super.paused(mediaPlayer)
                    scope.launch(Dispatchers.Main) {
                        component.playIcon.value = Icons.Default.PlayArrow
                    }
                }

                override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
                    super.timeChanged(mediaPlayer, newTime)
                    scope.launch(Dispatchers.Main) {
                        component.position.value = newTime
                    }
                }

                override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
                    super.lengthChanged(mediaPlayer, newLength)
                    scope.launch(Dispatchers.Main) {
                        component.length.value = newLength
                    }
                }
            })

            component.playPauseListener = {
                if (mediaPlayerComponent.mediaPlayer()?.status()?.isPlaying == true) {
                    mediaPlayerComponent.mediaPlayer()?.controls()?.pause()
                } else {
                    mediaPlayerComponent.mediaPlayer()?.controls()?.play()
                }
            }
            component.rewindListener = {
                mediaPlayerComponent.mediaPlayer()?.controls()?.skipTime(-10000)
            }
            component.forwardListener = {
                mediaPlayerComponent.mediaPlayer()?.controls()?.skipTime(10000)
            }
            component.seekListener = {
                mediaPlayerComponent.mediaPlayer()?.controls()?.setTime(it)
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                mediaPlayerComponent.mediaPlayer()?.release()
            }
        }
        SwingPanel(
            background = Color.Black,
            factory = {
                mediaPlayerComponent
            }
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "VLC must be installed on your system",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun MediaPlayerComponent.mediaPlayer(): MediaPlayer? {
    return when (this) {
        is CallbackMediaPlayerComponent -> mediaPlayer()
        is EmbeddedMediaPlayerComponent -> mediaPlayer()
        else -> null
    }
}

private fun isMacOs(): Boolean {
    val os = System.getProperty("os.name", "generic").lowercase(Locale.ENGLISH)
    return os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0
}