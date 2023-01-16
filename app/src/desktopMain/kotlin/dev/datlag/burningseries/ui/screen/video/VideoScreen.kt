package dev.datlag.burningseries.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.LocalStringRes

@Composable
actual fun VideoScreen(component: VideoComponent) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                backgroundColor = Color.Black,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = {
                        component.onGoBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = LocalStringRes.current.back
                        )
                    }
                },
                title = {
                    val episode by component.episode.collectAsState(component.episode.value)

                    Text(
                        text = episode.title,
                        maxLines = 1,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Box(modifier = Modifier.weight(1F).background(Color.Black)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    VideoPlayer(
                        component = component
                    )
                }
            }
            VideoControls(component)
        }
    }
}