package dev.datlag.burningseries.ui.screen.initial.series.activate.component

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.common.withIOContext
import dev.datlag.burningseries.common.withMainContext
import dev.datlag.burningseries.model.BSUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebView(url: String, modifier: Modifier, onScraped: (String) -> Unit) {
    val state = rememberWebViewState(url)
    var webView = remember<WebView?> { null }
    val scrapingJs = SharedRes.assets.scrape_hoster_android.readText(LocalContext.current)


    com.google.accompanist.web.WebView(
        state = state,
        modifier = modifier,
        captureBackPresses = true,
        client = WebViewClient(
            allowedHosts = setOf(BSUtil.HOST_BS_TO)
        ),
        onCreated = {
            it.settings.allowFileAccess = false
            it.settings.javaScriptEnabled = true
            it.settings.javaScriptCanOpenWindowsAutomatically = false
            it.settings.mediaPlaybackRequiresUserGesture = true
            webView = it
        }
    )

    LaunchedEffect(webView) {
        withIOContext {
            do {
                delay(3000)
                withMainContext {
                    webView?.evaluateJavascript(scrapingJs) { result ->
                        result?.let(onScraped)
                    }
                }
            } while (isActive && webView != null)
        }
    }
}