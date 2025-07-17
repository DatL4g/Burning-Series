package dev.datlag.mimasu.extension.ui.navigation.aniworld

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import dev.datlag.mimasu.extension.ui.navigation.burningseries.WebViewClient

@Composable
fun AniWorld() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        WebView(
            state = rememberWebViewState("https://aniworld.to"),
            modifier = Modifier.padding(contentPadding).fillMaxSize(),
            captureBackPresses = true,
            client = WebViewClient(
                allowedHosts = setOf("aniworld.to")
            ),
            onCreated = {
                it.settings.allowFileAccess = false
                it.settings.javaScriptEnabled = true
                it.settings.javaScriptCanOpenWindowsAutomatically = false
                it.settings.mediaPlaybackRequiresUserGesture = true
            }
        )
    }
}