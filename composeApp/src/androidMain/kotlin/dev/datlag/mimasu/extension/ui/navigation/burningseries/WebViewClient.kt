package dev.datlag.mimasu.extension.ui.navigation.burningseries

import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.google.accompanist.web.AccompanistWebViewClient

data class WebViewClient(
    private val allowedHosts: Set<String> = emptySet()
) : AccompanistWebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return if (request?.url?.host != null) {
            !allowedHosts.contains(request.url.host)
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
    }
}
