package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.LocalWindow
import dev.datlag.burningseries.common.toDuration
import dev.datlag.burningseries.keyEventListener
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

@Composable
fun VideoControls(component: VideoComponent) {
    val window = LocalWindow.current

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = {
            component.rewind()
        }) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = LocalStringRes.current.rewind10,
                tint = Color.White
            )
        }
        IconButton(onClick = {
            component.triggerPlayPause()
        }) {
            Icon(
                imageVector = component.playIcon.subscribeAsState().value,
                contentDescription = null,
                tint = Color.White
            )
        }
        IconButton(onClick = {
            component.forward()
        }) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = LocalStringRes.current.forward10,
                tint = Color.White
            )
        }
        val pos by component.position.subscribeAsState()
        val length by component.length.subscribeAsState()
        val progress = try {
            pos * 100 / length
        } catch (ignored: Throwable) { 0 }.toFloat()
        var changeableProgress by remember { mutableStateOf(progress) }

        Text(
            text = pos.toDuration(),
            textAlign = TextAlign.Center
        )
        Slider(
            modifier = Modifier.weight(1F),
            value = changeableProgress,
            onValueChange = {
                changeableProgress = it
            },
            onValueChangeFinished = {
                component.seekTo(min(max(0, (length * (changeableProgress / 100F)).roundToLong()), length))
            },
            valueRange = 0F..100F,
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
        IconButton(onClick = {
            if (window.placement == WindowPlacement.Fullscreen) {
                window.placement = WindowPlacement.Floating
            } else {
                window.placement = WindowPlacement.Fullscreen
            }
        }) {
            Icon(
                imageVector = if (window.placement == WindowPlacement.Fullscreen) {
                    Icons.Default.FullscreenExit
                } else {
                    Icons.Default.Fullscreen
                },
                contentDescription = LocalStringRes.current.fullscreen,
                tint = Color.White
            )
        }
    }
}