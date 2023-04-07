package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.collectAsStateSafe
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.model.common.containsKey
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.ui.custom.collapsingtoolbar.CollapsingToolbarScaffoldScopeInstance.align
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.lang3.SystemUtils
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
        val videoStreams by component.videoStreams.collectAsStateSafe()
        val initialPos by component.initialPosition.collectAsStateSafe { component.initialPosition.getValueBlocking(0) }
        var streamListPos by remember(videoStreams) { mutableStateOf(0) }
        var srcListPos by remember(streamListPos) { mutableStateOf(0) }

        SideEffect {
            applyHeaders(videoStreams[streamListPos].header, mediaPlayerComponent.mediaPlayer())
            mediaPlayerComponent.mediaPlayer()?.media()?.play(videoStreams[streamListPos].srcList[srcListPos])
            mediaPlayerComponent.mediaPlayer()?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                override fun error(mediaPlayer: MediaPlayer?) {
                    super.error(mediaPlayer)
                    var play = false
                    if (videoStreams[streamListPos].srcList.size - 1 > srcListPos) {
                        srcListPos++
                        play = true
                    } else {
                        if (videoStreams.size - 1 > streamListPos) {
                            streamListPos++
                            srcListPos = 0
                            play = true
                        }
                    }

                    if (play) scope.launch(Dispatchers.Main) {
                        mediaPlayer?.media()?.play(videoStreams[streamListPos].srcList[srcListPos])
                        applyHeaders(videoStreams[streamListPos].header, mediaPlayerComponent.mediaPlayer())
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

                override fun opening(mediaPlayer: MediaPlayer?) {
                    super.opening(mediaPlayer)
                    scope.launch(Dispatchers.Main) {
                        component.seekTo(initialPos)
                    }
                }

                override fun finished(mediaPlayer: MediaPlayer?) {
                    super.finished(mediaPlayer)
                    component.playNextEpisode()
                }
            })
        }
        LaunchedEffect(mediaPlayerComponent) {
            component.playListener = {
                mediaPlayerComponent.mediaPlayer()?.controls()?.play()
            }
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
        SwingPanel(
            background = Color.Black,
            factory = {
                mediaPlayerComponent
            }
        )
        DisposableEffect(mediaPlayerComponent) {
            onDispose {
                mediaPlayerComponent.mediaPlayer()?.release()
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val strings = LocalStringRes.current

            Spacer(modifier = Modifier.weight(1F))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = strings.vlcMustBeInstalled,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Button(onClick = {
                strings.openInBrowser(Constants.VLC_DOWNLOAD_URL)
            }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = strings.downloadNow,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = strings.downloadNow)
            }
            Spacer(modifier = Modifier.weight(1F))
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
    return os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0 || SystemUtils.IS_OS_MAC
}

private fun applyHeaders(headers: Map<String, String>, mediaPlayer: MediaPlayer?) {
    if (headers.containsKey("Referer", true)) {
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