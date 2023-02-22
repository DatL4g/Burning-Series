package dev.datlag.burningseries.ui.custom

import dev.datlag.burningseries.common.isStarted
import dev.datlag.burningseries.model.common.contains
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kotlinx.coroutines.*
import java.net.URI

class JFXWebView(
    private val initialUrl: String,
    private val allowedHosts: Set<String> = setOf(),
    private val onLoading: () -> Unit,
    private val onError: () -> Unit
) : JFXPanel() {

    private lateinit var webView: WebView

    init {
        Platform.runLater(::initializeJavaFXScene)
    }

    private fun initializeJavaFXScene() = runCatching {
        webView = WebView()
        webView.engine.isJavaScriptEnabled = true
        webView.engine.setOnError { onError() }
        webView.engine.locationProperty().addListener { _, _, new ->
            if (!allowedHosts.contains(URI(new).host, true)) {
                webView.engine.loadWorker.cancel()
            }
        }
        val scene = Scene(webView)
        loadUrl(initialUrl)
        setScene(scene)
    }

    fun loadUrl(url: String) {
        if (::webView.isInitialized) {
            try {
                webView.engine.load(url)
            } catch (ignored: Throwable) {
                Platform.runLater {
                    webView.engine.load(url)
                }
            }
            onLoading()
        }
    }

    fun evaluateJavascript(js: String, result: (Any?) -> Unit) {
        if (::webView.isInitialized) {
            Platform.runLater {
                val value = runCatching {
                    webView.engine.executeScript(js)
                }.getOrNull()
                result(value)
            }
        } else {
            result(null)
        }
    }
}