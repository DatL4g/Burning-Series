package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.datlag.burningseries.common.toDuration
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomControls(
    isVisible: Boolean,
    progressFlow: StateFlow<Long>,
    lengthFlow: StateFlow<Long>,
    modifier: Modifier = Modifier,
    onSeekChanged: (Long) -> Unit
) {
    val progress by progressFlow.collectAsStateWithLifecycle()
    val length by lengthFlow.collectAsStateWithLifecycle()

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut()
    ) {
        BottomAppBar(
            modifier = modifier,
            containerColor = Color.Black.copy(alpha = 0.5F),
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                var showSeek by remember { mutableStateOf(true) }
                var changedSeek by remember { mutableLongStateOf(progress) }

                val displayProgress = remember(showSeek, progress, changedSeek) {
                    if (showSeek) {
                        changedSeek
                    } else {
                        progress
                    }
                }

                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = displayProgress.toFloat(),
                    valueRange = 0F..length.toFloat(),
                    onValueChange = {
                        showSeek = true
                        changedSeek = it.toLong()
                    },
                    onValueChangeFinished = {
                        onSeekChanged(changedSeek)
                        showSeek = false
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "${displayProgress.toDuration()} - ${length.toDuration()}")
                }
            }
        }
    }
}