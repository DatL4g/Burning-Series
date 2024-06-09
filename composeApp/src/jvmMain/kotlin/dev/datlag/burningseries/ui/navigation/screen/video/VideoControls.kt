package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import dev.datlag.burningseries.LocalWindow
import dev.datlag.burningseries.common.toDuration

@Composable
fun VideoControls(mediaPlayer: MediaPlayer) {
    BottomAppBar(
        containerColor = Color.Black,
        contentColor = Color.White
    ) {
        val isPlaying by mediaPlayer.isPlaying
        val time by remember {
            derivedStateOf { mediaPlayer.time.value }
        }
        val length by remember {
            derivedStateOf { mediaPlayer.length.value }
        }
        val isMuted by mediaPlayer.isMuted
        val window = LocalWindow.current
        var originalWindowPlacement = remember { window.placement }

        IconButton(
            onClick = {
                mediaPlayer.rewind()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.FastRewind,
                contentDescription = null
            )
        }
        IconButton(
            onClick = {
                if (isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.play()
                }
            }
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Rounded.Pause
                } else {
                    Icons.Rounded.PlayArrow
                },
                contentDescription = null
            )
        }
        IconButton(
            onClick = {
                mediaPlayer.forward()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.FastForward,
                contentDescription = null
            )
        }
        Text(
            text = time.toDuration(),
            textAlign = TextAlign.Center
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
            textAlign = TextAlign.Center
        )
        if (window.placement == WindowPlacement.Fullscreen) {
            IconButton(
                onClick = {
                    window.placement = originalWindowPlacement
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.FullscreenExit,
                    contentDescription = null
                )
            }
        } else {
            IconButton(
                onClick = {
                    originalWindowPlacement = window.placement
                    window.placement = WindowPlacement.Fullscreen
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = null
                )
            }
        }
        IconButton(
            onClick = {
                if (isMuted) {
                    mediaPlayer.unmute()
                } else {
                    mediaPlayer.mute()
                }
            }
        ) {
            Icon(
                imageVector = if (isMuted) {
                    Icons.AutoMirrored.Rounded.VolumeOff
                } else {
                    Icons.AutoMirrored.Rounded.VolumeUp
                },
                contentDescription = null
            )
        }
        Slider(
            value = mediaPlayer.volume.value,
            onValueChange = {
                mediaPlayer.unmute()
                mediaPlayer.setVolume(it)
            },
            valueRange = 0F..100F,
            modifier = Modifier.width(100.dp)
        )

        DisposableEffect(window) {
            onDispose {
                window.placement = originalWindowPlacement
            }
        }
    }
}