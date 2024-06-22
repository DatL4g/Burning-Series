package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.util.UnstableApi
import dev.datlag.burningseries.common.toDuration
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.StateFlow

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomControls(
    isVisible: Boolean,
    playerWrapper: PlayerWrapper,
    modifier: Modifier = Modifier,
) {
    val isFinished by playerWrapper.isFinished.collectAsStateWithLifecycle()
    val progress by playerWrapper.progress.collectAsStateWithLifecycle()
    val length by playerWrapper.length.collectAsStateWithLifecycle()

    AnimatedVisibility(
        modifier = modifier.safeDrawingPadding(),
        visible = isVisible || isFinished,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut()
    ) {
        BottomAppBar(
            modifier = modifier,
            containerColor = Color.Black.copy(alpha = 0.5F),
            contentColor = Color.White
        ) {
            val source = remember { MutableInteractionSource() }
            val dragging by source.collectIsDraggedAsState()
            var changingProgress by remember { mutableLongStateOf(progress) }
            val displayProgress = remember(dragging, progress, changingProgress) {
                if (dragging) {
                    changingProgress
                } else {
                    progress
                }
            }

            Text(
                text = displayProgress.toDuration(),
                maxLines = 1
            )
            Slider(
                modifier = Modifier.weight(1F),
                value = displayProgress.toFloat(),
                valueRange = 0F..length.toFloat(),
                onValueChange = {
                    if (dragging) {
                        changingProgress = it.toLong()
                        playerWrapper.showControls()
                    }
                },
                onValueChangeFinished = {
                    if (dragging) {
                        playerWrapper.seekTo(changingProgress)
                    }
                },
                interactionSource = source
            )
            Text(
                text = length.toDuration(),
                maxLines = 1
            )
        }
    }
}