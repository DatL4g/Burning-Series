package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import dev.datlag.burningseries.LocalResources
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.ui.screen.activate.ActivateComponent
import kotlinx.coroutines.*
import java.io.InputStream
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.getMemberAsString
import dev.datlag.burningseries.common.isStarted
import netscape.javascript.JSObject

@Suppress("NewApi")
@Composable
actual fun WebView(component: ActivateComponent) {
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    fun loadJavascript(): InputStream {
        return resources.getResourcesAsInputStream(Resources.JAVASCRIPT_SCRAPE_HOSTER) ?: InputStream.nullInputStream()
    }

    val _javascriptInput = remember { loadJavascript() }
    val javascriptInput = if (_javascriptInput.available() > 0) _javascriptInput else loadJavascript()
    val _javascript = remember { String(javascriptInput.readBytes()).trim() }
    val javascript = _javascript.ifEmpty { String(javascriptInput.readBytes()).trim() }

    var isOpen by remember { mutableStateOf(isStarted) }
    if (!isOpen) {
        scope.launch(Dispatchers.IO) {
            while (isActive && withContext(Dispatchers.Main) { !isOpen }) {
                withContext(Dispatchers.Main) {
                    if (isStarted) {
                        isOpen = true
                    }
                }
                delay(1000)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isOpen) {
            Text(text = LocalStringRes.current.waitComponentInit)
        } else {
            Text(text = LocalStringRes.current.activateWindowOpenedText)
        }
    }

    if (isOpen) {
        Window(
            onCloseRequest = {
                component.onGoBack()
            },
            state = WindowState(),
            visible = true,
            title = LocalStringRes.current.activateWindow,
            icon = null,
            undecorated = false,
            transparent = false,
            resizable = true,
            enabled = true,
            focusable = true,
            alwaysOnTop = true,
            onPreviewKeyEvent = { false },
            onKeyEvent = { false }
        ) {
            SwingPanel(
                background = MaterialTheme.colorScheme.background,
                factory = {
                    JFXWebView(
                        initialUrl = Constants.getBurningSeriesUrl(component.episode.href),
                        allowedHosts = setOf(Constants.HOST_BS_TO),
                        onLoading = { component.setStatus(Status.LOADING) },
                        onError = { component.setStatus(Status.ERROR.CLIENT) }
                    )
                },
                update = { webView ->
                    scope.launch(Dispatchers.IO) {
                        do {
                            delay(2000)
                            withContext(Dispatchers.Main) {
                                webView.evaluateJavascript(javascript) { result ->
                                    if (result != null) {
                                        val jsObject = result as? JSObject
                                        if (jsObject != null) {
                                            val href = jsObject.getMemberAsString("href")
                                            val url = jsObject.getMemberAsString("url")
                                            if (!href.isNullOrEmpty() && !url.isNullOrEmpty()) {
                                                component.saveScrapedData(href, url)
                                            }
                                        } else {
                                            val jsString = (result as? String)?.trim()
                                            if (!jsString.isNullOrEmpty() && !jsString.equals("null", true) && !jsString.equals("undefined", true)) {
                                                runCatching {
                                                    component.saveScrapedData(jsString)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } while (isActive && isOpen)
                    }
                }
            )
        }
    }
}