package dev.datlag.burningseries.ui.screen.initial.series.activate.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebContent
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.common.withIOContext
import dev.datlag.burningseries.common.withMainContext
import dev.datlag.burningseries.other.CEFState
import dev.datlag.burningseries.other.LocalCEFInitialization
import dev.datlag.burningseries.ui.custom.state.BrowserState
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun WebView(url: String, modifier: Modifier, onScraped: (String) -> Unit) {
    val cefInitState by LocalCEFInitialization.current

    if (cefInitState is CEFState.INITIALIZED) {
        val client = remember { KCEF.newClientBlocking() }
        val browser = remember { client.createBrowser(url) }
        val scrapingJs = SharedRes.assets.scrape_hoster.readText()

        SwingPanel(
            background = MaterialTheme.colorScheme.background,
            factory = {
                browser.uiComponent
            },
            modifier = modifier.padding(8.dp)
        )

        LaunchedEffect(browser) {
            withIOContext {
                do {
                    delay(3000)
                    withMainContext {
                        browser.evaluateJavaScript(scrapingJs) {
                            if (it != null) {
                                onScraped(it)
                            }
                        }
                    }
                } while (isActive)
            }
        }
    } else {
        BrowserState(SharedRes.strings.browser_initializing)
    }
}