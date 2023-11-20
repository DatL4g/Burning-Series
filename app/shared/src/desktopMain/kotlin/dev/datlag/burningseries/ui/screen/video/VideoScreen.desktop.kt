package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import dev.datlag.burningseries.common.withMainContext
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.MediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer

@Composable
actual fun VideoScreen(component: VideoComponent) {
    val foundVlc = NativeDiscovery().discover()

    if (foundVlc) {
        val mediaPlayer = remember {
            if (SystemUtils.IS_OS_MAC) {
                CallbackMediaPlayerComponent()
            } else {
                EmbeddedMediaPlayerComponent()
            }
        }

        SwingPanel(
            background = Color.Black,
            modifier = Modifier.fillMaxSize(),
            factory = {
                mediaPlayer
            }
        )

        val streamList = remember { component.streams }

        var streamIndex by remember(streamList) { mutableIntStateOf(0) }
        var sourceIndex by remember(streamIndex) { mutableIntStateOf(0) }
        val url = remember(streamIndex, sourceIndex) { streamList[streamIndex].list[sourceIndex] }
        val headers = remember(streamIndex) {
            streamList[streamIndex].headers
        }

        val eventListener = remember { object : MediaPlayerEventAdapter() {
            override fun error(mediaPlayer: MediaPlayer?) {
                super.error(mediaPlayer)

                if (streamList[streamIndex].list.size - 1 > sourceIndex) {
                    sourceIndex++
                } else if (streamList.size - 1 > streamIndex) {
                    streamIndex++
                }
            }
        } }

        LaunchedEffect(mediaPlayer, eventListener) {
            mediaPlayer.mediaPlayer()?.events()?.addMediaPlayerEventListener(eventListener)
        }

        LaunchedEffect(mediaPlayer, headers) {
            applyHeaders(headers, mediaPlayer.mediaPlayer())
        }

        LaunchedEffect(mediaPlayer, url) {
            withMainContext {
                mediaPlayer.mediaPlayer()?.media()?.play(url)
            }
        }

        DisposableEffect(mediaPlayer) {
            onDispose {
                mediaPlayer.mediaPlayer()?.release()
            }
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

private fun applyHeaders(headers: Map<String, String>, mediaPlayer: MediaPlayer?) {
    if (headers.containsKey("Referer")) {
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