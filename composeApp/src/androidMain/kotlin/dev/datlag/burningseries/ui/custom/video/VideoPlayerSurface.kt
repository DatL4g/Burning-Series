package dev.datlag.burningseries.ui.custom.video

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dev.datlag.burningseries.ui.custom.video.pip.enterPIPMode
import dev.datlag.burningseries.ui.custom.video.pip.isActivityStatePipMode

@OptIn(UnstableApi::class)
@Composable
internal fun VideoPlayerSurface(
    modifier: Modifier = Modifier,
    defaultPlayerView: PlayerView,
    player: ExoPlayer,
    usePlayerController: Boolean,
    handleLifecycle: Boolean,
    enablePip: Boolean,
    surfaceResizeMode: ResizeMode,
    onPipEntered: () -> Unit = { },
    autoDispose: Boolean = true
) {
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    val context = LocalContext.current

    var isPendingPipMode by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = {
            defaultPlayerView.apply {
                useController = usePlayerController
                resizeMode = surfaceResizeMode.toPlayerViewResizeMode()
                setBackgroundColor(Color.BLACK)
            }
        }
    )
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (handleLifecycle) {
                        player.pause()
                    }

                    if (enablePip && player.playWhenReady) {
                        isPendingPipMode = true

                        Handler(Looper.getMainLooper()).post {
                            enterPIPMode(context, defaultPlayerView)
                            onPipEntered()

                            Handler(Looper.getMainLooper()).postDelayed({
                                isPendingPipMode = false
                            }, 500)
                        }
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (handleLifecycle) {
                        player.play()
                    }

                    if (enablePip && player.playWhenReady) {
                        defaultPlayerView.useController = usePlayerController
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    val isPopMode = context.isActivityStatePipMode()

                    if (handleLifecycle || (enablePip && isPopMode && !isPendingPipMode)) {
                        player.stop()
                    }
                }

                else -> { }
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            if (autoDispose) {
                player.release()
                lifecycle.removeObserver(observer)
            }
        }
    }
}