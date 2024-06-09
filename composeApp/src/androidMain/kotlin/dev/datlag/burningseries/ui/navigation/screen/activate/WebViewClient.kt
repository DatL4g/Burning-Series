package dev.datlag.burningseries.ui.navigation.screen.activate

import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.kevinnzou.web.AccompanistWebViewClient
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

data class WebViewClient(
    private val allowedHosts: ImmutableSet<String> = persistentSetOf()
) : AccompanistWebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return if (request?.url?.host != null) {
            !allowedHosts.contains(request.url.host)
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
    }
}
