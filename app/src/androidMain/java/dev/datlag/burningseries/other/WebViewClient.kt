package dev.datlag.burningseries.other

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import okhttp3.OkHttpClient

data class WebViewClient(
    private val allowedHosts: Set<String> = setOf(),
    private val client: OkHttpClient
) : android.webkit.WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return try {
            WebViewInterceptor(client, request)
        } catch (ignored: Throwable) { null } ?: super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return if (request?.url?.host != null) {
            !allowedHosts.contains(request.url.host)
        } else {
            true
        }
    }
}
