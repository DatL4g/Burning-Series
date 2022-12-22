package dev.datlag.burningseries.other

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.Charset

object WebViewInterceptor {

    operator fun invoke(client: OkHttpClient, webResourceRequest: WebResourceRequest?): WebResourceResponse? {
        val url = webResourceRequest?.url?.toString()
        val headers = webResourceRequest?.requestHeaders?.toHeaders()

        val newRequest = url?.let {
            Request.Builder()
                .url(it)
                .apply {
                    if (headers != null) {
                        headers(headers)
                    }
                }
                .build()
        }

        val response = newRequest?.let { client.newCall(it).execute() }

        return response?.let {
            WebResourceResponse(
                it.body?.contentType()?.let { type -> "${type.type}/${type.subtype}" },
                it.body?.contentType()?.charset(Charset.defaultCharset())?.name(),
                it.code,
                it.message,
                it.headers.toMap(),
                it.body?.byteStream()
            )
        }
    }
}