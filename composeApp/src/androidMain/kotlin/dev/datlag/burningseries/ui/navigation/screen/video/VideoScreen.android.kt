package dev.datlag.burningseries.ui.navigation.screen.video

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MediaType
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import com.google.android.gms.cast.framework.CastState
import dev.datlag.burningseries.common.isConnectedOrConnecting
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.ui.custom.video.pip.enterPIPMode
import dev.datlag.burningseries.ui.custom.video.pip.isActivityStatePipMode
import dev.datlag.burningseries.ui.theme.SchemeTheme
import dev.datlag.kast.ConnectionState
import dev.datlag.kast.Kast
import dev.datlag.nanoid.NanoIdUtils
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
actual fun VideoScreen(component: VideoComponent) {
    val streamList = remember { component.streams.toImmutableList() }
    var streamIndex by remember(streamList) { mutableIntStateOf(0) }
    val (url, headers) = remember(streamList, streamIndex) {
        streamList[streamIndex]
    }
    var useLongTimeout by remember {
        mutableStateOf(false)
    }
    val metadata = remember(component.series, component.episode) {
        MediaMetadata.Builder()
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .setTitle(component.episode.mainTitle)
            .setSubtitle(component.episode.subTitle)
            .setGenre(component.series.firstGenre)
            .setAlbumTitle(component.series.mainTitle)
            .setArtworkUri(component.series.coverHref?.toUri())
            .setArtworkData(SchemeTheme.getByteArray(component.series), MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            .build()
    }
    val mediaItem = remember(streamList, url, metadata) {
        MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                metadata
            ).build()
    }

    val context = LocalContext.current
    val controller = rememberWindowController()

    val playerWrapper = remember(headers, useLongTimeout) {
        PlayerWrapper(
            context = context,
            castContext = Kast.castContext,
            startingPos = component.startingPos,
            startingLength = component.startingLength,
            headers = headers.toImmutableMap(),
            coverData = SchemeTheme.getByteArray(component.series),
            longTimeout = useLongTimeout,
            onError = {
                if (streamList.size - 1 > streamIndex) {
                    streamIndex++
                } else if (!useLongTimeout) {
                    useLongTimeout = true
                    streamIndex = 0
                }
            },
            onFirstFrame = {
                controller.isSystemBarsVisible = false
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.addWindowFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON and WindowManager.LayoutParams.FLAG_SECURE)
            },
            onProgressChange = {
                component.progress(it)
            },
            onLengthChange = {
                component.length(it)
            },
            onFinish = {
                component.ended()
            }
        )
    }

    LaunchedEffect(playerWrapper, mediaItem) {
        playerWrapper.play(mediaItem)
    }

    DisposableEffect(playerWrapper) {
        onDispose {
            playerWrapper.release()
        }
    }

    LaunchedEffect(playerWrapper) {
        playerWrapper.pollPosition()
    }

    val showControls by playerWrapper.showControls.collectAsStateWithLifecycle(false)
    var pressedBack by remember { mutableLongStateOf(0L) }
    val controlsVisible = showControls && !context.isActivityStatePipMode()

    BackHandler {
        val newTime = Clock.System.now().toEpochMilliseconds()
        if (pressedBack > 0L && newTime - pressedBack < 3000) {
            component.back()
        } else {
            pressedBack = newTime
        }
    }

    Scaffold(
        topBar = {
            TopControls(
                isVisible = controlsVisible,
                mainTitle = component.episode.mainTitle,
                subTitle = component.episode.subTitle ?: component.episode.convertedNumber?.let { "Episode $it" },
                playerWrapper = playerWrapper,
                onBack = component::back
            )
        },
        bottomBar = {
            BottomControls(
                isVisible = controlsVisible,
                playerWrapper = playerWrapper
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val nextEpisodeState by component.nextEpisode.collectAsStateWithLifecycle(EpisodeState.None)
            val isPlaying by playerWrapper.isPlaying.collectAsStateWithLifecycle()

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .focusable()
                    .onKeyEvent { key ->
                        playerWrapper.dispatchKey(controlsVisible, key)
                    },
                factory = { viewContext ->
                    PlayerView(viewContext)
                },
                update = { player ->
                    player.setOnClickListener {
                        playerWrapper.toggleControls()
                    }
                    player.isSoundEffectsEnabled = false
                    player.useController = false
                    player.keepScreenOn = true
                    player.player = playerWrapper.player
                }
            )
            CenterControls(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                isVisible = controlsVisible,
                isPlaying = isPlaying,
                nextState = nextEpisodeState,
                playerWrapper = playerWrapper,
                onReplayClick = {
                    playerWrapper.rewind()
                },
                onPauseToggle = {
                    playerWrapper.togglePlay()
                },
                onForwardClick = {
                    playerWrapper.forward()
                },
                onNext = component::next
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.isSystemBarsVisible = true
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            controller.clearWindowFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON and WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

tailrec fun Context.findWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findWindow()
    else -> null
}