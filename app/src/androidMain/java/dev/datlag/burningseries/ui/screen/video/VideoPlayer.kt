package dev.datlag.burningseries.ui.screen.video

import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.DefaultTimeBar
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.button.MaterialButton
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.R
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.ui.activity.KeyEventDispatcher
import dev.datlag.burningseries.ui.activity.PIPActions
import dev.datlag.burningseries.ui.activity.PIPEventDispatcher
import dev.datlag.burningseries.ui.activity.PIPModeListener
import dev.datlag.burningseries.ui.custom.RequireFullScreen
import dev.datlag.burningseries.ui.custom.extendedPlayer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

val LocalCastContext = compositionLocalOf<CastContext?> { null }

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(component: VideoComponent) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val castContext = LocalCastContext.current
    val episode by component.episode.collectAsStateSafe()

    val extendedPlayer = remember(context) {
        context.extendedPlayer(scope) {
            streamFlow(component.videoStreams)
            castContext(castContext)
            position(
                component.initialPosition.stateIn(
                    scope,
                    SharingStarted.WhileSubscribed(),
                    component.initialPosition.getValueBlocking(0)
                )
            )
        }
    }

    val strings = LocalStringRes.current
    val buttonShape = MaterialTheme.shapes.medium.toLegacyShape()
    val buttonColors = ButtonDefaults.legacyButtonTintList(MaterialTheme.colorScheme.primaryContainer)
    val progressColor = MaterialTheme.colorScheme.primary.toArgb()

    RequireFullScreen()

    LaunchedEffect(extendedPlayer) {
        component.playListener = {
            extendedPlayer.play()
        }
        component.playPauseListener = {
            extendedPlayer.triggerPlay()
        }
        component.forwardListener = {
            extendedPlayer.seekForward()
        }
        component.rewindListener = {
            extendedPlayer.seekBack()
        }
        component.seekListener = {
            extendedPlayer.seekTo(it)
        }
    }

    DisposableEffect(
        AndroidView(
            factory = {
                extendedPlayer
            },
            update = {
                val controls = it.controlsView
                val backButton = controls.findViewById<ImageButton>(R.id.backButton)
                val title = controls.findViewById<TextView>(R.id.title)
                val skipButton = it.findViewById<MaterialButton>(R.id.skip)
                val progress = controls.findViewById<DefaultTimeBar>(R.id.exo_progress)

                backButton.setOnClickListener {
                    component.onGoBack()
                }
                title.text = episode.title
                skipButton.shapeAppearanceModel = buttonShape
                skipButton.backgroundTintList = buttonColors
                progress.setPlayedColor(progressColor)
                progress.setScrubberColor(progressColor)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && it.context.findActivity()?.isInPictureInPictureMode == true) {
                    controls.visibility = View.GONE
                }
            }
        )
    ) {
        onDispose {
            extendedPlayer.release()
            KeyEventDispatcher = { null }
            PIPEventDispatcher = { null }
            PIPModeListener = { }
            PIPActions = { null }
        }
    }
}