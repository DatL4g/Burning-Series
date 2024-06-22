package dev.datlag.burningseries.ui.navigation.screen.activate

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewState
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.compose.withMainContext
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun ActivateScreen(component: ActivateComponent) {
    val dialogState by component.dialog.subscribeAsState()

    dialogState.child?.instance?.render()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Toolbar(component)
        }
    ) { padding ->
        val state = rememberWebViewState(BSUtil.getBurningSeriesLink(component.episode.href))
        val scope = rememberCoroutineScope()

        WebView(
            state = state,
            modifier = Modifier.padding(padding).fillMaxSize(),
            captureBackPresses = true,
            client = WebViewClient(
                allowedHosts = persistentSetOf(BSUtil.HOST_BS_TO)
            ),
            onCreated = {
                it.settings.allowFileAccess = false
                it.settings.javaScriptEnabled = true
                it.settings.javaScriptCanOpenWindowsAutomatically = false
                it.settings.mediaPlaybackRequiresUserGesture = true

                scope.launchIO {
                    it.scrape(
                        js = String(Res.readBytes("files/scrape_hoster_android.js")),
                        onScraped = component::onScraped
                    )
                }
            }
        )
    }
}

private suspend fun WebView.scrape(js: String?, onScraped: (String?) -> Unit) {
    while (currentCoroutineContext().isActive && !js.isNullOrBlank()) {
        delay(3000)
        withMainContext {
            this@scrape.evaluateJavascript(js) { result ->
                onScraped(result)
            }
        }
    }
}