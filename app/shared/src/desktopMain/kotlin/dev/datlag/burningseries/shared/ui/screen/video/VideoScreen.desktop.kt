package dev.datlag.burningseries.shared.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.AppIO
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.openInBrowser
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
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(SharedRes.strings.vlc_required),
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        component.back()
                    }
                ) {
                    Text(text = stringResource(SharedRes.strings.back))
                }
                Button(
                    onClick = {
                        AppIO.VLC_ORG.openInBrowser()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(SharedRes.strings.download)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.download))
                }
            }
        }
    }
}