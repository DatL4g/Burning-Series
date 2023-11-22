package dev.datlag.burningseries.ui.screen.initial.series.activate.component

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.google.accompanist.web.AccompanistWebViewClient

data class WebViewClient(
    private val allowedHosts: Set<String> = setOf()
) : AccompanistWebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return if (request?.url?.host != null) {
            !allowedHosts.contains(request.url.host)
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
    }
}
