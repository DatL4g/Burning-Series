package dev.datlag.burningseries.shared.ui.screen.video

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.media.session.MediaSession.Token
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageButton
import android.widget.TextView
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.cast.CastPlayer
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
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.cast.framework.CastState
import dev.datlag.burningseries.model.common.scopeCatching
import dev.datlag.burningseries.shared.R
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.findWindow
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.withIOContext
import dev.datlag.burningseries.shared.common.withMainContext
import dev.datlag.burningseries.shared.ui.*
import dev.datlag.kast.ConnectionState
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.nanoid.NanoIdUtils
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.*
import kotlin.random.Random

val PseudoRandom = Random(12345) // pseudo random as secure random is not needed

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
actual fun VideoScreen(component: VideoComponent) {
    val context = LocalContext.current
    val castContext = Kast.castContext
    val connectionState by Kast.connectionState.collectAsStateWithLifecycle()
    val castButtonConnected = remember(connectionState) {
        when (connectionState) {
            is ConnectionState.DISCONNECTED -> false
            else -> true
        }
    }
    val dialogState by component.dialog.subscribeAsState()

    val streamList = remember { component.streams }

    var streamIndex by remember(streamList) { mutableIntStateOf(0) }
    var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
    val headers by remember(streamIndex) {
        mutableStateOf(streamList[streamIndex].headers)
    }
    val mediaItem = remember(streamList, streamIndex, sourceIndex) {
        MediaItem.fromUri(streamList[streamIndex].list[sourceIndex])
    }
    val startingPos by component.startingPos.collectAsStateWithLifecycle()

    val castState by remember(castContext) { mutableStateOf(castContext?.castState) }
    val casting by remember(castState) { mutableStateOf(castState == CastState.CONNECTED || castState == CastState.CONNECTING) }
    var cast by remember(casting) { mutableStateOf(casting) }
    val useCastPlayer = remember(cast) { cast }

    val sessionListener = remember { object : SessionAvailabilityListener {
        override fun onCastSessionAvailable() {
            cast = true
        }

        override fun onCastSessionUnavailable() {
            cast = false
        }
    } }

    var subtitles by remember { mutableStateOf(emptyList<VideoComponent.Subtitle>()) }
    val selectedLanguage by component.selectedSubtitle.collectAsStateWithLifecycle()

    fun updateSubtitles(tracks: Tracks) {
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
        }.flatten().toSet().mapNotNull {
            var title = Locale(it).displayLanguage
            if (title == it) {
                val code = it.split("[-_]".toRegex()).firstOrNull() ?: it
                title = scopeCatching {
                    Locale.Builder().setLanguage(code).build()
                }.getOrNull()?.displayName ?: scopeCatching {
                    Locale.forLanguageTag(code)
                }.getOrNull()?.displayName ?: return@mapNotNull null
            }
            if (title != it) {
                title += " ($it)"
            }

            VideoComponent.Subtitle(
                code = it,
                title = title
            )
        }

        subtitles = languages
    }

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

            updateSubtitles(tracks)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if (playbackState == Player.STATE_ENDED) {
                component.ended()
            }
        }
    } }

    val castPlayer = remember(castContext) {
        if (castContext != null) {
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

    val usingPlayer = remember(castPlayer, localPlayer, useCastPlayer) {
        if (useCastPlayer) {
            castPlayer ?: localPlayer
        } else {
            localPlayer
        }
    }

    val session = remember(usingPlayer) {
        val nanoId = NanoIdUtils.randomNanoId(random = PseudoRandom)
        MediaSession.Builder(context, usingPlayer).setId(nanoId).build()
    }

    DisposableEffect(session) {
        onDispose {
            session.release()
        }
    }

    val mediaStyle = remember(session) {
        Notification.MediaStyle().setMediaSession(session.sessionCompatToken.token as Token)
    }
    val channelName = stringResource(SharedRes.strings.channel_videoplayer_title)
    val channelText = stringResource(SharedRes.strings.channel_videoplayer_text)
    val channel = remember(channelName, channelText) {
        NotificationChannelCompat.Builder("Cast", NotificationManager.IMPORTANCE_LOW)
            .setName(channelName)
            .setDescription(channelText)
            .build()
    }

    LaunchedEffect(channel) {
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    val notification = remember(mediaStyle, channel) {
        (100..200).random() to Notification.Builder(context, channel.id)
            .setStyle(mediaStyle)
            .setSmallIcon(SmallIcon)
            .build()
    }

    LaunchedEffect(notification) {
        if (!NotificationPermission) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationPermission = true
            }
        }

        if (NotificationPermission) {
            NotificationManagerCompat.from(context).notify(notification.first, notification.second)
        }
    }

    DisposableEffect(notification) {
        onDispose {
            NotificationManagerCompat.from(context).cancel(notification.first)
        }
    }

    LaunchedEffect(usingPlayer, mediaItem) {
        val media = if (usingPlayer is CastPlayer) {
            val mimeType = mediaItem.localConfiguration?.mimeType ?: MimeTypes.VIDEO_UNKNOWN
            mediaItem.buildUpon().setMimeType(mimeType).build()
        } else {
            mediaItem
        }
        usingPlayer.setMediaItem(media, startingPos)
        usingPlayer.prepare()

        updateSubtitles(usingPlayer.currentTracks)

        withIOContext {
            do {
                delay(3000)
                withMainContext {
                    component.lengthUpdate(usingPlayer.duration)
                    component.progressUpdate(usingPlayer.currentPosition)
                }
            } while (isActive)
        }
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
    val episode by component.episode.collectAsStateWithLifecycle()

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
            val mediaRouteButton = controls.findViewById<ImageButton>(R.id.cast_button)
            val subtitleButton = controls.findViewById<ImageButton>(R.id.subtitle)

            backButton.setOnClickListener {
                component.back()
            }

            val initialIcon = if (castButtonConnected) {
                R.drawable.baseline_cast_connected_24
            } else {
                R.drawable.baseline_cast_24
            }
            mediaRouteButton.setImageResource(initialIcon)
            mediaRouteButton.setOnClickListener {
                usingPlayer.pause()
                component.selectCast()
            }

            subtitleButton.setOnClickListener {
                playerView.player?.pause()
                component.selectSubtitle(subtitles)
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
            val title = controls.findViewById<TextView>(R.id.title)
            val subtitleButton = controls.findViewById<ImageButton>(R.id.subtitle)
            val progress = controls.findViewById<DefaultTimeBar>(R.id.exo_progress)
            val mediaRouteButton = controls.findViewById<ImageButton>(R.id.cast_button)

            playerView.player = usingPlayer

            title.text = episode.episodeTitle

            val castIcon = if (castButtonConnected) {
                R.drawable.baseline_cast_connected_24
            } else {
                R.drawable.baseline_cast_24
            }
            mediaRouteButton.setImageResource(castIcon)

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
            Kast.unselect(UnselectReason.stopped)
        }
    }

    dialogState.child?.instance?.render()
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