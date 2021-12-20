package de.datlag.burningseries.ui.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayInputStream

class AdBlockWebViewClient(
    private val allowedHosts: Set<String> = setOf()
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

    companion object {
        private val emptyResponse = ByteArrayInputStream(String().toByteArray())
        private const val emptyResponseType = "text/plain"
        private const val emptyResponseCharType = "utf-8"
        private val emptyWebResponse = WebResourceResponse(emptyResponseType, emptyResponseCharType, emptyResponse)
    }
}