package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState

@Composable
actual fun PlatformWebView(state: WebViewState, navigator: WebViewNavigator, modifier: Modifier) {
    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier
    )
}