package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.ui.custom.video.VideoPlayer
import dev.datlag.burningseries.ui.custom.video.uri.VideoPlayerMediaItem
import kotlinx.collections.immutable.toImmutableList

@Composable
actual fun VideoScreen(component: VideoComponent) {
    VideoPlayer(
        modifier = Modifier.fillMaxSize(),
        mediaItems = component.streams.first().sources.map { VideoPlayerMediaItem.NetworkMediaItem(it) }.toImmutableList()
    )
}