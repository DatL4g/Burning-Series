package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.ui.dialog.subtitle.SubtitleComponent
import dev.datlag.burningseries.ui.dialog.subtitle.SubtitleDialog
import kotlinx.coroutines.*

@Composable
actual fun VideoScreen(component: VideoComponent) {
    val dialogState = component.dialog.subscribeAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoPlayer(component)
    }

    dialogState.value.overlay?.also { (config, instance) ->
        when (config) {
            is DialogConfig.Subtitle -> {
                SubtitleDialog(instance as SubtitleComponent)
            }
        }
    }
}