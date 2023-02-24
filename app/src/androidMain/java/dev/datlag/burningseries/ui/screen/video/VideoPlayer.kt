package dev.datlag.burningseries.ui.screen.video

import android.app.RemoteAction
import android.content.pm.ActivityInfo
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.*
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.R
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.other.Logger
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.activity.KeyEventDispatcher
import dev.datlag.burningseries.ui.activity.PIPActions
import dev.datlag.burningseries.ui.activity.PIPEventDispatcher
import dev.datlag.burningseries.ui.activity.PIPModeListener
import dev.datlag.burningseries.ui.custom.RequireFullScreen
import dev.datlag.burningseries.ui.custom.RequireScreenOrientation
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
    val episode by component.episode.collectAsStateSafe()
    val videoStreams by component.videoStreams.collectAsStateSafe()
    var streamListPos by remember(videoStreams) { mutableStateOf(0) }
    var srcListPos by remember(streamListPos) { mutableStateOf(0) }
    val stream = videoStreams[streamListPos].srcList[srcListPos]

    val strings = LocalStringRes.current
    var appliedInitialPosition by remember { mutableStateOf(false) }
    val buttonShape = MaterialTheme.shapes.medium.toLegacyShape()
    val buttonColors = ButtonDefaults.legacyButtonTintList(MaterialTheme.colorScheme.primaryContainer)
    val progressColor = MaterialTheme.colorScheme.primary.toArgb()
    val initialPos by component.initialPosition.collectAsStateSafe { component.initialPosition.getValueBlocking(0) }

    RequireFullScreen()

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

                        if (videoStreams[streamListPos].srcList.size - 1 > srcListPos) {
                            srcListPos++
                        } else {
                            if (videoStreams.size - 1 > streamListPos) {
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

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        super.onIsLoadingChanged(isLoading)

                        if (isLoading) {
                            component.playIcon.value = Icons.Default.MoreHoriz
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        super.onRenderedFirstFrame()

                        if (!appliedInitialPosition) {
                            component.seekTo(initialPos)
                            appliedInitialPosition = true
                        }
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

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)

                        if (playbackState == Player.STATE_ENDED) {
                            component.playNextEpisode()
                        }
                    }
                })
                playWhenReady = true
                prepare()

                MediaSession.Builder(context, this).build()
            }
    }

    LaunchedEffect(exoPlayer) {
        component.playListener = {
            exoPlayer.play()
        }
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
    }

    DisposableEffect(
        AndroidView(factory = {
            val root = LayoutInflater.from(it).inflate(R.layout.video_player, null, false)
            root.apply {
                val playerView = findViewById<PlayerView>(R.id.player)
                val controls = playerView.findViewById<View>(R.id.exoplayer_controls)

                playerView.player = exoPlayer
                KeyEventDispatcher = { event ->
                    event?.let { ev -> playerView.dispatchKeyEvent(ev) }
                }
                PIPEventDispatcher = { true }
                PIPModeListener = { isInPIP ->
                    if (isInPIP) {
                        controls.visibility = View.GONE
                    } else {
                        controls.visibility = View.VISIBLE
                    }
                }

                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
        }, update = {
            it.apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }

            val controls = it.findViewById<PlayerView>(R.id.player).findViewById<View>(R.id.exoplayer_controls)
            val backButton = controls.findViewById<ImageButton>(R.id.backButton)
            val title = controls.findViewById<TextView>(R.id.title)
            val skipButton = it.findViewById<MaterialButton>(R.id.skip)
            val progress = controls.findViewById<DefaultTimeBar>(R.id.exo_progress)

            backButton.setOnClickListener {
                component.onGoBack()
            }
            title.text = episode.title
            skipButton.shapeAppearanceModel = buttonShape
            skipButton.backgroundTintList = buttonColors
            progress.setPlayedColor(progressColor)
            progress.setScrubberColor(progressColor)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && it.context.findActivity()?.isInPictureInPictureMode == true) {
                controls.visibility = View.GONE
            }
        })
    ) {
        onDispose {
            exoPlayer.release()
            KeyEventDispatcher = { null }
            PIPEventDispatcher = { null }
            PIPModeListener = { }
            PIPActions = { null }
        }
    }
    RunOnce(exoPlayer, {
        exoPlayer.mediaItemCount <= 0 || exoPlayer.playbackState == Player.STATE_IDLE
    }) {
        exoPlayer.setMediaItem(MediaItem.fromUri(stream))
        exoPlayer.prepare()
    }
}