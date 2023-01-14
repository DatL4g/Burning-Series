package dev.datlag.burningseries.other

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.*
import androidx.core.net.toUri
import dev.datlag.burningseries.model.common.contains
import okhttp3.OkHttpClient

data class WebViewClient(
    private val allowedHosts: Set<String> = setOf(),
    private val client: OkHttpClient,
    private val onLoading: () -> Unit,
    private val onError: () -> Unit
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

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        if (allowedHosts.contains(request?.url?.host, true)) {
            onError()
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        if (allowedHosts.contains(request?.url?.host, true)) {
            onError()
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        if (allowedHosts.contains(error?.url?.toUri()?.host, true)) {
            onError()
            handler?.proceed()
        }
    }
}
