package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.other.CEFState
import dev.datlag.burningseries.other.LocalCEFInitialization
import dev.datlag.burningseries.ui.custom.state.BrowserState

@Composable
actual fun PlatformWebView(state: WebViewState, navigator: WebViewNavigator, modifier: Modifier) {
    val cefInitState by LocalCEFInitialization.current

    if (cefInitState is CEFState.INITIALIZED) {
        WebView(
            state = state,
            navigator = navigator,
            modifier = modifier
        )
    } else {
        BrowserState(SharedRes.strings.browser_initializing)
    }
}