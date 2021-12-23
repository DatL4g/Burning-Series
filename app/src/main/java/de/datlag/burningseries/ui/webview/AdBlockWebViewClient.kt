package de.datlag.burningseries.ui.webview

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.*
import androidx.core.net.toUri
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayInputStream

@Obfuscate
class AdBlockWebViewClient(
    private val allowedHosts: Set<String> = setOf(),
    private val startedLoading: (() -> Unit)? = null,
    private val finishedLoading: (() -> Unit)? = null,
    private val receivedError: ((Uri?) -> Unit)? = null
) : WebViewClient() {

    val adBlockList: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return if (adBlockList.value.contains(":::::${request?.url?.host}")) {
            emptyWebResponse
        } else {
            super.shouldInterceptRequest(view, request)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return if (request?.url?.host != null) {
            !allowedHosts.contains(request.url.host)
        } else {
            true
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        startedLoading?.invoke()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        finishedLoading?.invoke()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (allowedHosts.contains(request?.url?.host)) {
            receivedError?.invoke(request?.url)
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        if (allowedHosts.contains(error?.url?.toUri()?.host)) {
            receivedError?.invoke(error?.url?.toUri())
        }
    }

    companion object {
        private val emptyResponse = ByteArrayInputStream(String().toByteArray())
        private const val emptyResponseType = "text/plain"
        private const val emptyResponseCharType = "utf-8"
        private val emptyWebResponse = WebResourceResponse(emptyResponseType, emptyResponseCharType, emptyResponse)
    }
}