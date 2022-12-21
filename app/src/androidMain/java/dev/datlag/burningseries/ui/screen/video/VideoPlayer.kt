package dev.datlag.burningseries.ui.screen.video

import android.widget.FrameLayout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.*
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dev.datlag.burningseries.other.Logger
import kotlinx.coroutines.*

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(component: VideoComponent) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extractorFactory = remember {
        DefaultExtractorsFactory().setTsExtractorFlags(
            FLAG_ALLOW_NON_IDR_KEYFRAMES and FLAG_DETECT_ACCESS_UNITS and FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
        )
    }
    val dataSourceFactory = remember {
        DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true))
    }
    var streamListPos by remember { mutableStateOf(0) }
    var srcListPos by remember { mutableStateOf(0) }
    val stream = component.videoStreams[streamListPos].srcList[srcListPos]

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .apply {
                setSeekBackIncrementMs(10000)
                setSeekForwardIncrementMs(10000)
                setPauseAtEndOfMediaItems(true)
                setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory, extractorFactory))
            }
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)

                        if (component.videoStreams[streamListPos].srcList.size - 1 > srcListPos) {
                            srcListPos++
                        } else {
                            if (component.videoStreams.size - 1 > streamListPos) {
                                streamListPos++
                                srcListPos = 0
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)

                        component.playIcon.value = if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        super.onRenderedFirstFrame()

                        component.length.value = (this@apply as ExoPlayer).duration
                        scope.launch(Dispatchers.IO) {
                            while(this.isActive) {
                                withContext(Dispatchers.Main) {
                                    component.position.value = (this@apply as ExoPlayer).currentPosition
                                }
                                delay(500)
                            }
                        }
                    }
                })
                playWhenReady = true
                prepare()
            }
    }

    SideEffect {
        component.playPauseListener = {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        }
        component.forwardListener = {
            exoPlayer.seekForward()
        }
        component.rewindListener = {
            exoPlayer.seekBack()
        }
        component.seekListener = {
            exoPlayer.seekTo(it)
        }
        exoPlayer.setMediaItem(MediaItem.fromUri(stream))
        exoPlayer.prepare()
    }

    DisposableEffect(
        AndroidView(factory = {
            PlayerView(context).apply {
                hideController()
                useController = false
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
        })
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}