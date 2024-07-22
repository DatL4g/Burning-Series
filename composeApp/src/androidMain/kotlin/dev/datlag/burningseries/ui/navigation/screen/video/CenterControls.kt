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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import dev.datlag.burningseries.common.drawProgress
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.skeo.DirectLink
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformIcon
import dev.datlag.tooling.compose.platform.PlatformIconButton
import dev.datlag.tooling.compose.platform.localContentColor
import dev.datlag.tooling.compose.platform.rememberIsTv
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableCollection

@ExperimentalComposeUiApi
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
    onNext: (Series.Episode, ImmutableCollection<DirectLink>) -> Unit
) {
    val isFinished by playerWrapper.isFinished.collectAsStateWithLifecycle()
    val (replay, play, forward) = remember { FocusRequester.createRefs() }

    var focusPlay by remember { mutableStateOf(true) }

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
            PlatformIconButton(
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.5F),
                    shape = CircleShape
                ).focusRequester(replay).focusProperties {
                    next = play
                },
                onClick = onReplayClick
            ) {
                PlatformIcon(
                    imageVector = Icons.Rounded.FastRewind,
                    contentDescription = null,
                    tint = if (Platform.rememberIsTv()) {
                        Platform.localContentColor()
                    } else {
                        Color.White
                    }
                )
            }

            PlatformIconButton(
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.5F),
                    shape = CircleShape
                ).focusRequester(play).focusProperties {
                    previous = replay
                    next = forward
                },
                onClick = onPauseToggle
            ) {
                PlatformIcon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = if (Platform.rememberIsTv()) {
                        Platform.localContentColor()
                    } else {
                        Color.White
                    }
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
                PlatformIconButton(
                    modifier = Modifier.background(
                        color = Color.Black.copy(alpha = 0.5F),
                        shape = CircleShape
                    ).focusRequester(forward).focusProperties {
                        previous = play
                    },
                    onClick = {
                        onNext(nextState.episode, nextState.results)
                    }
                ) {
                    PlatformIcon(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawProgress(
                                if (Platform.rememberIsTv()) {
                                    Platform.localContentColor()
                                } else {
                                    Color.White
                                },
                                animatedProgress
                            ).padding(8.dp),
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = if (Platform.rememberIsTv()) {
                            Platform.localContentColor()
                        } else {
                            Color.White
                        }
                    )
                }
            } else {
                PlatformIconButton(
                    modifier = Modifier.background(
                        color = Color.Black.copy(alpha = 0.5F),
                        shape = CircleShape
                    ).focusRequester(forward).focusProperties {
                        previous = play
                    },
                    onClick = onForwardClick,
                    enabled = !isFinished
                ) {
                    PlatformIcon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = null,
                        tint = if (Platform.rememberIsTv()) {
                            Platform.localContentColor()
                        } else {
                            Color.White
                        }
                    )
                }
            }
        }

        SideEffect {
            if (focusPlay) {
                play.requestFocus()
                focusPlay = false
            }
        }

        DisposableEffect(focusPlay) {
            onDispose {
                focusPlay = true
            }
        }
    }
}