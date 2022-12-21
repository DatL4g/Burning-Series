package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*

@Composable
actual fun VideoScreen(component: VideoComponent) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoPlayer(component)
        VideoControls(component)
    }
}