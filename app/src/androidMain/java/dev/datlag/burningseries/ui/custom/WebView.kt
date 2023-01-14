package dev.datlag.burningseries.ui.custom

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dev.datlag.burningseries.LocalResources
import dev.datlag.burningseries.ui.screen.activate.ActivateComponent
import kotlinx.coroutines.*
import java.io.InputStream
import dev.datlag.burningseries.R
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.other.*

@Composable
actual fun WebView(component: ActivateComponent) {
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val context = LocalContext.current

    fun loadJavascript(): InputStream {
        return resources.getResourcesAsInputStream(Resources.JAVASCRIPT_SCRAPE_HOSTER) ?: context.resources.openRawResource(R.raw.scrape_hoster)
    }

    val _javascriptInput = remember { loadJavascript() }
    val javascriptInput = if (_javascriptInput.available() > 0) _javascriptInput else loadJavascript()
    val _javascript = remember { String(javascriptInput.readBytes()).trim() }
    val javascript = _javascript.ifEmpty { String(javascriptInput.readBytes()).trim() }

    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient(
                allowedHosts = setOf(Constants.HOST_BS_TO),
                client = component.client,
                onLoading = { component.setStatus(Status.LOADING) },
                onError = { component.setStatus(Status.ERROR.CLIENT) }
            )
            clearHistory()
            clearCache(true)
            clearMatches()
            clearFormData()

            settings.apply {
                allowFileAccess = false
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = false
                mediaPlaybackRequiresUserGesture = true
            }

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            loadUrl(Constants.getBurningSeriesUrl(component.episode.href))
        }
    }) { webView ->
        scope.launch(Dispatchers.IO) {
            while (isActive && withContext(Dispatchers.Main) { webView.isAttachedToWindow }) {
                withContext(Dispatchers.Main) {
                    webView.evaluateJavascript(javascript) { result ->
                        if (result != null && result.isNotEmpty() && !result.equals("null", true)) {
                            component.saveScrapedData(result)
                        }
                    }
                }
                delay(2000)
            }
        }
    }
}