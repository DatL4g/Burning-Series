package dev.datlag.burningseries.ui.screen.initial.series.activate.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.burningseries.common.withIOContext
import dev.datlag.burningseries.common.withMainContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun WebView(url: String, scrapingJs: String, modifier: Modifier, onScraped: (String) -> Unit) {
    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator()

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