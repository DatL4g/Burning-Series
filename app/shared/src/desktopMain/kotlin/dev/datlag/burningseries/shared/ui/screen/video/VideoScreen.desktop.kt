package dev.datlag.burningseries.shared.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery

@Composable
actual fun VideoScreen(component: VideoComponent) {
    val foundVlc = NativeDiscovery().discover()

    if (foundVlc) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val episode by component.episode.collectAsStateWithLifecycle()

                IconButton(
                    onClick = {
                        component.back()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(SharedRes.strings.back),
                        tint = Color.White
                    )
                }
                Text(
                    text = episode.episodeTitle,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val mediaPlayer = VideoPlayer(
                component = component,
                modifier = Modifier.fillMaxWidth().weight(1F)
            )

            if (mediaPlayer != null) {
                VideoControls(
                    mediaPlayer = mediaPlayer
                )
            }
        }
    }
}