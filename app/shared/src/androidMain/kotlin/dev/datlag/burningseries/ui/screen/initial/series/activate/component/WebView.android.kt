package dev.datlag.burningseries.ui.screen.initial.series.activate.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.common.withIOContext
import dev.datlag.burningseries.common.withMainContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun WebView(url: String, modifier: Modifier, onScraped: (String) -> Unit) {
    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator()
    val scrapingJs = SharedRes.assets.scrape_hoster_android.readText(LocalContext.current)

    com.multiplatform.webview.web.WebView(
        state = state,
        modifier = modifier,
        navigator = navigator
    )

    LaunchedEffect(navigator) {
        withIOContext {
            do {
                delay(3000)
                withMainContext {
                    navigator.evaluateJavaScript(scrapingJs) {
                        onScraped(it)
                    }
                }
            } while (isActive)
        }
    }
}