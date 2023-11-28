package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.withIOContext
import dev.datlag.burningseries.shared.common.withMainContext
import dev.datlag.burningseries.shared.other.CEFState
import dev.datlag.burningseries.shared.other.LocalCEFInitialization
import dev.datlag.burningseries.shared.ui.custom.state.BrowserState
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun WebView(url: String, modifier: Modifier, onScraped: (String) -> Unit) {
    val cefInitState by LocalCEFInitialization.current

    if (cefInitState is CEFState.INITIALIZED) {
        val client = remember { KCEF.newClientBlocking() }
        val browser = remember { client.createBrowser(url) }
        val scrapingJs = SharedRes.assets.scrape_hoster_cef.readText()

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