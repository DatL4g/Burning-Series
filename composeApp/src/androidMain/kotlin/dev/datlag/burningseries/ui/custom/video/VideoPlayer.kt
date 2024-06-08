package dev.datlag.burningseries.ui.custom.video

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import dev.datlag.burningseries.ui.custom.video.cache.VideoPlayerCacheManager
import dev.datlag.burningseries.ui.custom.video.controller.VideoPlayerControllerConfig
import dev.datlag.burningseries.ui.custom.video.pip.enterPIPMode
import dev.datlag.burningseries.ui.custom.video.uri.VideoPlayerMediaItem
import dev.datlag.nanoid.NanoIdUtils
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.coroutines.delay
import kotlin.random.Random

private val PseudoRandom = Random((10000..12345).random())
private val Alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    mediaItems: ImmutableCollection<VideoPlayerMediaItem>,
    modifier: Modifier = Modifier,
    handleLifecycle: Boolean = true,
    autoPlay: Boolean = true,
    usePlayerController: Boolean = true,
    controllerConfig: VideoPlayerControllerConfig = VideoPlayerControllerConfig.Default,
    seekBeforeMilliSeconds: Long = 10000L,
    seekAfterMilliSeconds: Long = 10000L,
    resizeMode: ResizeMode = ResizeMode.Fit,
    onCurrentTimeChanged: (Long) -> Unit = {},
    enablePip: Boolean = true,
    enablePipWhenBackPressed: Boolean = false,
    handleAudioFocus: Boolean = true,
    playerInstance: ExoPlayer.() -> Unit = {},
) {
    val context = LocalContext.current
    var currentTime by remember { mutableLongStateOf(0L) }
    var mediaSession = remember<MediaSession?> { null }

    val player = remember(context) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()

        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(seekBeforeMilliSeconds)
            .setSeekForwardIncrementMs(seekAfterMilliSeconds)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                handleAudioFocus
            ).apply {
                val cache = VideoPlayerCacheManager.cache()
                if (cache != null) {
                    val cacheDataSourceFactory = CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))

                    setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                }
            }
            .build().also(playerInstance)
    }

    val defaultPlayerView = remember(context) {
        PlayerView(context)
    }

    BackHandler(enablePip && enablePipWhenBackPressed) {
        enterPIPMode(context, defaultPlayerView)
        player.play()
    }

    LaunchedEffect(player) {
        while (true) {
            delay(1000)

            if (currentTime != player.currentPosition) {
                onCurrentTimeChanged(currentTime)
            }

            currentTime = player.currentPosition
        }
    }

    LaunchedEffect(usePlayerController) {
        defaultPlayerView.useController = usePlayerController
    }

    LaunchedEffect(player) {
        defaultPlayerView.player = player
    }

    LaunchedEffect(mediaItems, player) {
        mediaSession?.release()
        mediaSession = MediaSession.Builder(
            context,
            ForwardingPlayer(player)
        ).setId(
            NanoIdUtils.randomNanoId(
                random = PseudoRandom,
                alphabet = Alphabet.toCharArray()
            )
        ).build()

        val exoPlayerMediaItems = mediaItems.map {
            val uri = it.toUri(context)

            MediaItem.Builder().apply {
                setUri(uri)
                setMediaMetadata(it.mediaMetadata)
                setMimeType(it.mimeType)
                setDrmConfiguration(
                    if (it is VideoPlayerMediaItem.NetworkMediaItem) {
                        it.drmConfiguration
                    } else {
                        null
                    }
                )
            }.build()
        }

        player.setMediaItems(exoPlayerMediaItems)
        player.prepare()

        if (autoPlay) {
            player.play()
        }
    }

    LaunchedEffect(controllerConfig) {
        controllerConfig.applyToExoPlayerView(defaultPlayerView)
    }

    VideoPlayerSurface(
        modifier = modifier,
        defaultPlayerView = defaultPlayerView,
        player = player,
        usePlayerController = usePlayerController,
        handleLifecycle = handleLifecycle,
        enablePip = enablePip,
        surfaceResizeMode = resizeMode
    )
}