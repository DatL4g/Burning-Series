package dev.datlag.mimasu.extension

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.htmlunit.BrowserVersion
import org.htmlunit.NicelyResynchronizingAjaxController
import org.htmlunit.Page
import org.htmlunit.SilentCssErrorHandler
import org.htmlunit.WebClient
import org.htmlunit.WebRequest
import org.htmlunit.html.HtmlPage
import org.htmlunit.javascript.SilentJavaScriptErrorListener
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            App {
                var html by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        html = runCatching { fetch() }.getOrNull() ?: "Failed fetching"
                    }
                }

                Text(
                    text = html.ifBlank { "Loading..." },
                    color = Color.White
                )
            }
        }
    }

    private suspend fun fetch(): String? {
        val client = WebClient(BrowserVersion.BEST_SUPPORTED).also {
            it.options.apply {
                isJavaScriptEnabled = false
                isCssEnabled = false
                isRedirectEnabled = true
            }
            it.ajaxController = NicelyResynchronizingAjaxController()
            it.cssErrorHandler = SilentCssErrorHandler()
            it.javaScriptErrorListener = SilentJavaScriptErrorListener()
        }
        val request = WebRequest(URL("https://filmpalast.to/"))
        val page: Page = client.getPage(request)
        val document = when {
            page.isHtmlPage -> (page as HtmlPage).asXml()
            else -> page.webResponse.contentAsString
        }

        client.javaScriptEngine.shutdown()
        client.close()
        client.cache.clear()
        return document
    }

}