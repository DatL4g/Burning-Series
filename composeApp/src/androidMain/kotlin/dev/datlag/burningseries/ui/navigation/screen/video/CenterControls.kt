package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CenterControls(
    isVisible: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onReplayClick: () -> Unit,
    onPauseToggle: () -> Unit,
    onForwardClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.5F),
                    shape = CircleShape
                ),
                onClick = onReplayClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastRewind,
                    contentDescription = null
                )
            }

            IconButton(
                modifier = Modifier.size(56.dp).background(
                    color = Color.Black.copy(alpha = 0.5F),
                    shape = CircleShape
                ),
                onClick = onPauseToggle
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null
                )
            }

            IconButton(
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.5F),
                    shape = CircleShape
                ),
                onClick = onForwardClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastForward,
                    contentDescription = null
                )
            }
        }
    }
}