package dev.datlag.mimasu.extension.ui.navigation.burningseries

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import dev.datlag.mimasu.extension.composeapp.generated.resources.Res
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.withMainContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun BurningSeries() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        val state = rememberWebViewState(BurningSeries.homePage)
        val scope = rememberCoroutineScope()

        WebView(
            state = state,
            modifier = Modifier.padding(contentPadding).fillMaxSize(),
            captureBackPresses = true,
            client = WebViewClient(
                allowedHosts = setOf(BurningSeries.HOST)
            ),
            onCreated = {
                it.settings.allowFileAccess = false
                it.settings.javaScriptEnabled = true
                it.settings.javaScriptCanOpenWindowsAutomatically = false
                it.settings.mediaPlaybackRequiresUserGesture = true

                scope.launchIO {
                    it.scrape(
                        js = Res.readBytes("files/scrape_hoster.js").decodeToString(),
                        onScraped = { episodeHref, result ->
                            Logger.e("Episode Href: $episodeHref, Result: $result")
                        }
                    )
                }
            }
        )
    }
}

private suspend fun WebView.scrape(js: String?, onScraped: (String?, String?) -> Unit) {
    while (currentCoroutineContext().isActive && !js.isNullOrBlank()) {
        delay(3000)
        withMainContext {
            val episodeHref = BurningSeries.matchingUrl(
                this@scrape.url,
                this@scrape.originalUrl
            )

            this@scrape.evaluateJavascript(js) { result ->
                val episode = episodeHref ?: BurningSeries.matchingUrl(
                    this@scrape.url,
                    this@scrape.originalUrl
                )

                onScraped(episode, result)
            }
        }
    }
}