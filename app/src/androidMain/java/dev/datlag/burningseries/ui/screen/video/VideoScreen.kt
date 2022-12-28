package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.datlag.burningseries.common.onClick
import kotlinx.coroutines.*

@Composable
actual fun VideoScreen(component: VideoComponent) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoPlayer(component)
    }
}