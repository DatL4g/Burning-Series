package de.datlag.burningseries.ui.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayInputStream

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
        receivedError?.invoke(request?.url)
    }

    companion object {
        private val emptyResponse = ByteArrayInputStream(String().toByteArray())
        private const val emptyResponseType = "text/plain"
        private const val emptyResponseCharType = "utf-8"
        private val emptyWebResponse = WebResourceResponse(emptyResponseType, emptyResponseCharType, emptyResponse)
    }
}