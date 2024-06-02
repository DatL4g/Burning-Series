package dev.datlag.burningseries.network

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.common.href
import dev.datlag.burningseries.network.common.parseGet
import dev.datlag.burningseries.network.common.src
import dev.datlag.burningseries.network.common.title
import dev.datlag.tooling.async.suspendCatching
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data object BurningSeries {

    private suspend fun document(client: HttpClient, url: String): Document? = suspendCatching {
        return@suspendCatching suspendCatching {
            Ksoup.parseGet(
                url = BSUtil.getBurningSeriesLink(url),
                client = client
            )
        }.getOrNull() ?: suspendCatching {
            Ksoup.parseGet(
                url = BSUtil.getBurningSeriesLink(url, true),
                client = client
            )
        }.getOrNull()
    }.getOrNull()

    private suspend fun latestSeries(client: HttpClient, document: Document) = coroutineScope {
        document.getElementById("newest_series")?.select("li")?.map {
            async {
                val seriesTitle = it.selectFirst("a")?.title()
                val seriesHref = it.selectFirst("a")?.href()?.let(BSUtil::fixSeriesHref)

                if (!seriesTitle.isNullOrBlank() && !seriesHref.isNullOrBlank()) {
                    val coverHref = cover(client, seriesHref)

                    Home.Series(
                        title = seriesTitle,
                        href = seriesHref,
                        coverHref = coverHref
                    )
                } else {
                    null
                }
            }
        }?.awaitAll()?.filterNotNull() ?: persistentListOf()
    }

    suspend fun home(client: HttpClient): Home? {
        val homeDoc = document(client, "") ?: return null
        val series = latestSeries(client, homeDoc)

        return if (series.isNotEmpty()) {
            Home(
                series = series.toImmutableSet()
            )
        } else {
            null
        }
    }

    private suspend fun cover(client: HttpClient, url: String): String? {
        val commonUrl = BSUtil.commonSeriesHref(url)
        val coverDoc = document(client, commonUrl)

        return coverDoc?.let { cover(it) }
    }

    private suspend fun cover(document: Document): String? {
        val seriesElement = document.getElementsByClass("serie").firstOrNull() ?: document.selectFirst(".serie")
        val allImages = seriesElement?.getElementsByTag("img")

        val cover = (allImages?.firstOrNull {
            it.attr("alt").equals("Cover", ignoreCase = true)
        } ?: allImages?.firstOrNull())?.src()

        return cover
    }
}