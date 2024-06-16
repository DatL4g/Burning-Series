package dev.datlag.burningseries.ui.navigation.screen.video

import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

private val PseudoRandom = Random((10000..12345).random())
private val Alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

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
    val startingPos = component.startingPos

    val playListener = remember { object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)

            if (streamList[streamIndex].sources.size -1 > sourceIndex) {
                sourceIndex++
            } else {
                streamIndex++
            }
        }
    } }

    val context = LocalContext.current
    val castContext = Kast.castContext
    val castState by remember(castContext) { mutableStateOf(castContext?.castState) }
    val casting by remember(castState) { mutableStateOf(castState == CastState.CONNECTED) }
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

    DisposableEffect(castPlayer) {
        onDispose {
            castPlayer?.release()
        }
    }

    val localPlayer = remember(context) {
        ExoPlayer.Builder(context).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
        }.build()
    }

    LaunchedEffect(localPlayer) {
        localPlayer.playWhenReady = true
        localPlayer.addListener(playListener)
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

    val mediaSession = remember(context, usingPlayer) {
        MediaSession.Builder(
            context,
            ForwardingPlayer(usingPlayer)
        ).setId(
            NanoIdUtils.randomNanoId(
                random = PseudoRandom,
                alphabet = Alphabet.toCharArray()
            )
        ).build()
    }

    DisposableEffect(mediaSession) {
        onDispose {
            mediaSession.release()
        }
    }

    LaunchedEffect(usingPlayer, mediaItem) {
        val media = if (usingPlayer is CastPlayer) {
            val mimeType = mediaItem.localConfiguration?.mimeType?.ifBlank { null } ?: MimeTypes.VIDEO_UNKNOWN
            mediaItem.buildUpon().setMimeType(mimeType).build()
        } else {
            mediaItem
        }

        usingPlayer.setMediaItem(media, startingPos)
        usingPlayer.prepare()

        withIOContext {
            do {
                delay(3000)
                withMainContext {
                    component.length(usingPlayer.duration)
                    component.progress(usingPlayer.currentPosition)
                }
            } while (isActive)
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        factory = { viewContext ->
            PlayerView(viewContext)
        },
        update = { playerView ->
            playerView.player = usingPlayer
        }
    )
}