package dev.datlag.burningseries.ui.navigation.screen.video

import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
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
import dev.datlag.burningseries.ui.custom.video.VideoPlayer
import dev.datlag.burningseries.ui.custom.video.uri.VideoPlayerMediaItem
import dev.datlag.kast.ConnectionState
import dev.datlag.kast.Kast
import dev.datlag.nanoid.NanoIdUtils
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
actual fun VideoScreen(component: VideoComponent) {
    val streamList = remember { component.streams.toImmutableList() }
    var streamIndex by remember(streamList) { mutableIntStateOf(0) }
    var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
    val headers by remember(streamIndex) {
        mutableStateOf(streamList[streamIndex].headers)
    }
    val metadata = remember(component.series, component.episode) {
        MediaMetadata.Builder()
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .setTitle(component.episode.mainTitle)
            .setSubtitle(component.episode.subTitle)
            .setGenre(component.series.firstGenre)
            .setAlbumTitle(component.series.mainTitle)
            .setArtworkUri(component.series.coverHref?.toUri())
            .build()
    }
    val mediaItem = remember(streamList, streamIndex, sourceIndex, metadata) {
        MediaItem.Builder()
            .setUri(streamList[streamIndex].sources.toImmutableList()[sourceIndex])
            .setMediaMetadata(
                metadata
            ).build()
    }

    val context = LocalContext.current

    val playerWrapper = remember {
        PlayerWrapper(
            context = context,
            castContext = Kast.castContext,
            startingPos = component.startingPos,
            onError = {
                if (streamList[streamIndex].sources.size -1 > sourceIndex) {
                    sourceIndex++
                } else if (streamList.size - 1 > streamIndex) {
                    streamIndex++
                }
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

    var showControls by remember { mutableStateOf(false) }
    LaunchedEffect(showControls) {
        withIOContext {
            if (showControls) {
                delay(3000)
                if (currentCoroutineContext().isActive && showControls) {
                    showControls = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopControls(
                isVisible = showControls,
                mainTitle = component.episode.mainTitle,
                subTitle = component.episode.subTitle ?: component.episode.convertedNumber?.let { "Episode: $it" },
                onBack = component::back
            )
        },
        bottomBar = {
            BottomControls(
                isVisible = showControls,
                progressFlow = playerWrapper.progress,
                lengthFlow = playerWrapper.length,
                onSeekChanged = {
                    playerWrapper.seekTo(it)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isPlaying by playerWrapper.isPlaying.collectAsStateWithLifecycle()

            AndroidView(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                factory = { viewContext ->
                    PlayerView(viewContext)
                },
                update = { playerView ->
                    playerView.setOnClickListener {
                        showControls = !showControls
                    }
                    playerView.useController = false
                    playerView.keepScreenOn = true
                    playerView.player = playerWrapper.player
                }
            )
            CenterControls(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                isVisible = showControls,
                isPlaying = isPlaying,
                onReplayClick = {
                    playerWrapper.rewind()
                },
                onPauseToggle = {
                    playerWrapper.togglePlay()
                },
                onForwardClick = {
                    playerWrapper.forward()
                }
            )
        }
    }
}