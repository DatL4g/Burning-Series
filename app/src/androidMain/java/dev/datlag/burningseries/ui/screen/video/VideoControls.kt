package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.SemiBlack
import dev.datlag.burningseries.common.findWindow
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.common.toDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

@Composable
fun VideoControls(component: VideoComponent) {
    val scope = rememberCoroutineScope()
    var displayControls by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        displayControls = false
    }

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures {
            displayControls = true
            scope.launch(Dispatchers.IO) {
                delay(3000)
                displayControls = false
            }
        }
    }) {
        if (displayControls) {
            TopAppBar(
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = {
                        component.onGoBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = LocalStringRes.current.back
                        )
                    }
                },
                title = {
                    Text(
                        text = component.episode.title,
                        maxLines = 1
                    )
                },
                modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.SemiBlack, Color.Transparent)
                    )
                ),
                elevation = 0.dp
            )

            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp).background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.SemiBlack)
                    )
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            }
        }
    }
}