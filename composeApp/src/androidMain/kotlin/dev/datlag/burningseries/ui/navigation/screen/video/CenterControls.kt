package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.skeo.Stream
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableCollection

@OptIn(UnstableApi::class)
@Composable
fun CenterControls(
    isVisible: Boolean,
    isPlaying: Boolean,
    nextState: EpisodeState,
    playerWrapper: PlayerWrapper,
    modifier: Modifier = Modifier,
    onReplayClick: () -> Unit,
    onPauseToggle: () -> Unit,
    onForwardClick: () -> Unit,
    onNext: (Series.Episode, ImmutableCollection<Stream>) -> Unit
) {
    val isFinished by playerWrapper.isFinished.collectAsStateWithLifecycle()

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible || isFinished,
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
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.5F),
                    shape = CircleShape
                ),
                onClick = onPauseToggle
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            val animatedProgress by animateFloatAsState(
                targetValue = if (isFinished && nextState is EpisodeState.SuccessStream) 1F else 0F,
                animationSpec = tween(
                    durationMillis = 10000,
                    easing = LinearEasing
                ),
                finishedListener = {
                    val success = (nextState as? EpisodeState.SuccessStream)

                    success?.let {
                        onNext(success.episode, success.results)
                    }
                }
            )
            if (isFinished && nextState is EpisodeState.SuccessStream) {
                IconButton(
                    modifier = Modifier.background(
                        color = Color.Black.copy(alpha = 0.5F),
                        shape = CircleShape
                    ),
                    onClick = {
                        onNext(nextState.episode, nextState.results)
                    }
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize().drawWithContent {
                            with(drawContext.canvas.nativeCanvas) {
                                val checkPoint = saveLayer(null, null)

                                drawContent()

                                drawRect(
                                    color = Color.White,
                                    size = Size(size.width * animatedProgress, size.height),
                                    blendMode = BlendMode.SrcOut
                                )
                                restoreToCount(checkPoint)
                            }
                        }.padding(8.dp),
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            } else {
                IconButton(
                    modifier = Modifier.background(
                        color = Color.Black.copy(alpha = 0.5F),
                        shape = CircleShape
                    ),
                    onClick = onForwardClick,
                    enabled = !isFinished
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}