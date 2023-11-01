package dev.datlag.burningseries.network.scraper

import dev.datlag.burningseries.model.Constants
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.common.getHref
import dev.datlag.burningseries.network.common.getTitle
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import ktsoup.KtSoupDocument
import ktsoup.KtSoupParser
import ktsoup.parseRemote
import ktsoup.setClient

data object BurningSeries {

    suspend fun getDocument(client: HttpClient, url: String): KtSoupDocument? = suspendCatching {
        KtSoupParser.setClient(client)

        return@suspendCatching suspendCatching {
            KtSoupParser.parseRemote(Constants.BurningSeries.getBurningSeriesLink(url))
        }.getOrNull() ?: suspendCatching {
            KtSoupParser.parseRemote(Constants.BurningSeries.getBurningSeriesLink(url, true))
        }.getOrNull()
    }.getOrNull()

    suspend fun getLatestSeries(document: KtSoupDocument) = coroutineScope {
        document.querySelectorAll("#newest_series li").map {
            async {
                val seriesTitle = it.querySelector("a")?.getTitle() ?: String()
                val seriesHref = it.querySelector("a")?.getHref() ?: String()

                if (seriesTitle.isNotEmpty() && seriesHref.isNotEmpty()) {
                    Home.Series(
                        title = seriesTitle,
                        href = seriesHref
                    )
                } else {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun testSeries(client: HttpClient) {
        val doc = getDocument(client, String())

        doc?.let {
            getLatestSeries(it).forEach(::println)
        }

        doc?.close()
    }
}