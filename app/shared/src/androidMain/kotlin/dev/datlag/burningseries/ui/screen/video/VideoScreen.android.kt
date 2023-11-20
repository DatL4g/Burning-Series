package dev.datlag.burningseries.ui.screen.video

import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.*
import androidx.media3.session.MediaSession
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import dev.datlag.burningseries.ui.KeyEventDispatcher
import dev.datlag.burningseries.ui.PIPActions
import dev.datlag.burningseries.ui.PIPEventDispatcher
import dev.datlag.burningseries.ui.PIPModeListener
import dev.datlag.burningseries.R
import dev.datlag.burningseries.common.findActivity
import dev.datlag.burningseries.common.findWindow
import io.github.aakira.napier.Napier
import java.util.Locale

val LocalCastContext = compositionLocalOf<CastContext?> { null }

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
actual fun VideoScreen(component: VideoComponent) {
    val context = LocalContext.current
    val castContext = LocalCastContext.current

    val streamList = remember { component.streams }

    var streamIndex by remember(streamList) { mutableIntStateOf(0) }
    var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
    val headers = remember(streamIndex) {
        streamList[streamIndex].headers
    }
    val mediaItem = remember(streamList, streamIndex, sourceIndex) {
        MediaItem.fromUri(streamList[streamIndex].list[sourceIndex])
    }

    val castState by remember(castContext) { mutableStateOf(castContext?.castState) }
    val casting by remember(castState) { mutableStateOf(castState == CastState.CONNECTED || castState == CastState.CONNECTING) }
    var cast by remember(casting) { mutableStateOf(casting) }
    val useCastPlayer by remember(headers, cast) { mutableStateOf(headers.isEmpty() && cast) }

    val sessionListener = remember { object : SessionAvailabilityListener {
        override fun onCastSessionAvailable() {
            if (headers.isEmpty()) {
                cast = true
            }
        }

        override fun onCastSessionUnavailable() {
            cast = false
        }
    } }

    var subtitles = remember { emptyList<Language>() }
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }

    val playListener = remember { object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)

            if (streamList[streamIndex].list.size - 1 > sourceIndex) {
                sourceIndex++
            } else if (streamList.size - 1 > streamIndex) {
                streamIndex++
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)

            val languages = tracks.groups.mapNotNull { group ->
                if (group.type != C.TRACK_TYPE_TEXT) {
                    return@mapNotNull null
                }

                val formats = (0 until group.length).map { index ->
                    group.getTrackFormat(index)
                }
                formats.mapNotNull { format ->
                    format.language
                }
            }.flatten().toSet().map {
                var title = Locale(it).displayLanguage
                if (title == it) {
                    title = Locale(it.split("[-_]".toRegex()).firstOrNull() ?: it).displayName
                }
                if (title != it) {
                    title += " ($it)"
                }

                Language(
                    code = it,
                    title = title
                )
            }

            subtitles = languages
        }
    } }

    val castPlayer = remember(castContext) {
        if (castContext != null) {
            CastPlayer(castContext)
            CastPlayer(castContext)
        } else {
            null
        }
    }

    LaunchedEffect(castPlayer) {
        castPlayer?.playWhenReady = true
        castPlayer?.setSessionAvailabilityListener(sessionListener)
        castPlayer?.addListener(playListener)
    }

    val extractorFactory = remember {
        DefaultExtractorsFactory().setTsExtractorFlags(
            FLAG_ALLOW_NON_IDR_KEYFRAMES and FLAG_DETECT_ACCESS_UNITS and FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
        )
    }

    val dataSource = remember(headers) {
        DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(headers)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
        )
    }

    val localPlayer = remember(context, dataSource) {
        ExoPlayer.Builder(context).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
            setMediaSourceFactory(DefaultMediaSourceFactory(dataSource, extractorFactory))
        }.build()
    }

    LaunchedEffect(localPlayer) {
        localPlayer.addListener(playListener)
        localPlayer.playWhenReady = true
    }

    DisposableEffect(localPlayer) {
        onDispose {
            localPlayer.release()
        }
    }

    val session = remember(localPlayer) {
        MediaSession.Builder(context, localPlayer).build()
    }

    DisposableEffect(session) {
        onDispose {
            session.release()
        }
    }

    val usingPlayer = remember(castPlayer, localPlayer, useCastPlayer) {
        if (useCastPlayer) {
            castPlayer ?: localPlayer
        } else {
            localPlayer
        }
    }

    LaunchedEffect(usingPlayer, mediaItem) {
        val media = if (usingPlayer is CastPlayer) {
            val mimeType = mediaItem.localConfiguration?.mimeType ?: MimeTypes.VIDEO_UNKNOWN
            mediaItem.buildUpon().setMimeType(mimeType).build()
        } else {
            mediaItem
        }
        usingPlayer.setMediaItem(media)
        usingPlayer.prepare()
    }

    LaunchedEffect(usingPlayer, subtitles, selectedLanguage) {
        val chosen = subtitles.firstOrNull { it.code == selectedLanguage?.code }
        usingPlayer.trackSelectionParameters = usingPlayer
            .trackSelectionParameters
            .buildUpon()
            .setPreferredTextLanguage(chosen?.code)
            .build()
    }

    DisposableEffect(usingPlayer) {
        onDispose {
            usingPlayer.release()
        }
    }

    val progressColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        factory = { viewContext ->
            val view = LayoutInflater.from(viewContext).inflate(R.layout.video_player, null, false)

            view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            view.setBackgroundColor(android.graphics.Color.BLACK)
            view.keepScreenOn = true

            val playerView = view.findViewById<PlayerView>(R.id.player)
            val controls = playerView.findViewById<View>(R.id.exoplayer_controls)
            val backButton = controls.findViewById<ImageButton>(R.id.back_button)
            val mediaRouteButton = controls.findViewById<MediaRouteButton>(R.id.cast_button)
            val subtitleButton = controls.findViewById<ImageButton>(R.id.subtitle)

            backButton.setOnClickListener {
                component.back()
            }

            CastButtonFactory.setUpMediaRouteButton(
                viewContext.findActivity() ?: viewContext,
                mediaRouteButton
            )

            subtitleButton.setOnClickListener {
                playerView.player?.pause()
            }

            KeyEventDispatcher = { event ->
                event?.let { playerView.dispatchKeyEvent(it) }
            }
            PIPEventDispatcher = { true }
            PIPModeListener = { isInPIP ->
                if (isInPIP) {
                    controls.visibility = View.GONE
                } else {
                    controls.visibility = View.VISIBLE
                }
            }

            view
        },
        update = { view ->
            val playerView = view.findViewById<PlayerView>(R.id.player)
            val controls = playerView.findViewById<View>(R.id.exoplayer_controls)
            val mediaRouteButton = controls.findViewById<MediaRouteButton>(R.id.cast_button)
            val subtitleButton = controls.findViewById<ImageButton>(R.id.subtitle)
            val progress = controls.findViewById<DefaultTimeBar>(R.id.exo_progress)

            playerView.player = usingPlayer

            mediaRouteButton.isEnabled = headers.isEmpty()

            if (subtitles.isNotEmpty()) {
                subtitleButton.visibility = View.VISIBLE
                subtitleButton.isEnabled = true
            } else {
                subtitleButton.visibility = View.INVISIBLE
                subtitleButton.isEnabled = false
            }

            progress.setPlayedColor(progressColor)
            progress.setScrubberColor(progressColor)
        }
    )

    RequireFullScreen()

    DisposableEffect(Unit) {
        onDispose {
            KeyEventDispatcher = { null }
            PIPEventDispatcher = { null }
            PIPModeListener = { }
            PIPActions = { null }
        }
    }
}

@Composable
private fun RequireFullScreen() {
    val window = LocalView.current.context.findWindow() ?: LocalContext.current.findWindow()
    val systemUiController = rememberSystemUiController(window)

    DisposableEffect(Unit) {
        val originalBehavior = systemUiController.systemBarsBehavior

        systemUiController.isSystemBarsVisible = false
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON and WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            systemUiController.isSystemBarsVisible = true
            systemUiController.systemBarsBehavior = originalBehavior
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON and WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Parcelize
data class Language(
    val code: String,
    val title: String
) : Parcelable