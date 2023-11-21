package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import dev.datlag.burningseries.LocalWindow
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.common.toDuration
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun VideoControls(
    mediaPlayer: MediaPlayer
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.Black)
    ) {
        val time by remember {
            derivedStateOf { mediaPlayer.time.value }
        }
        val length by remember {
            derivedStateOf { mediaPlayer.length.value }
        }
        val window = LocalWindow.current
        var originalPlacement = remember(window) { window.placement }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    mediaPlayer.rewind()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = stringResource(SharedRes.strings.rewind),
                    tint = Color.White
                )
            }
            if (mediaPlayer.isPlaying.value) {
                IconButton(
                    onClick = {
                        mediaPlayer.pause()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = stringResource(SharedRes.strings.pause),
                        tint = Color.White
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        mediaPlayer.play()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(SharedRes.strings.play),
                        tint = Color.White
                    )
                }
            }
            IconButton(
                onClick = {
                    mediaPlayer.forward()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = stringResource(SharedRes.strings.forward),
                    tint = Color.White
                )
            }
            Text(
                text = time.toDuration(),
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Slider(
                modifier = Modifier.weight(1F),
                value = time.toDouble().toFloat(),
                onValueChange = {
                    mediaPlayer.seekTo(it.toLong())
                },
                valueRange = 0F..length.toDouble().toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2F)
                )
            )
            Text(
                text = length.toDuration(),
                textAlign = TextAlign.Center,
                color = Color.White
            )
            if (window.placement == WindowPlacement.Fullscreen) {
                IconButton(
                    onClick = {
                        window.placement = originalPlacement
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = stringResource(SharedRes.strings.fullscreen),
                        tint = Color.White
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        originalPlacement = window.placement
                        window.placement = WindowPlacement.Fullscreen
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = stringResource(SharedRes.strings.fullscreen),
                        tint = Color.White
                    )
                }
            }
        }

        DisposableEffect(window) {
            onDispose {
                window.placement = originalPlacement
            }
        }
    }
}