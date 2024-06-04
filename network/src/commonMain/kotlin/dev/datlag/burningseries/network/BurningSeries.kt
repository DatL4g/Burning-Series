package dev.datlag.burningseries.network

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.common.firstClass
import dev.datlag.burningseries.network.common.firstTag
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

internal data object BurningSeries {

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

    private suspend fun latestEpisodes(client: HttpClient, document: Document) = coroutineScope {
        document.getElementById("newest_episodes")?.getElementsByTag("li")?.map { li ->
            async {
                val episodeLinkElement = li.firstTag("a")
                val episodeTitle = episodeLinkElement?.title()
                val episodeHref = episodeLinkElement?.href()?.let(BSUtil::fixSeriesHref)
                val episodeInfoElement = li.firstClass("info")
                val episodeInfo = episodeInfoElement?.text()
                val episodeFlagElements = episodeInfoElement?.getElementsByTag("i")

                val episodeInfoFlags: MutableList<Home.Episode.Flag> = mutableListOf()
                episodeFlagElements?.forEach { infoFlags ->
                    val flagClass = infoFlags.className()
                    val flagTitle = infoFlags.title()

                    if (flagClass.isNotBlank()) {
                        episodeInfoFlags.add(
                            Home.Episode.Flag(
                                clazz = flagClass,
                                title = flagTitle
                            )
                        )
                    }
                }

                if (!episodeTitle.isNullOrBlank() && !episodeHref.isNullOrBlank()) {
                    val coverHref = cover(client, episodeHref)

                    Home.Episode(
                        fullTitle = episodeTitle,
                        href = episodeHref,
                        info = episodeInfo?.ifBlank { null },
                        flags = episodeInfoFlags.toImmutableSet(),
                        coverHref = coverHref
                    )
                } else {
                    null
                }
            }
        }?.awaitAll()?.filterNotNull() ?: persistentListOf()
    }

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

    internal suspend fun home(client: HttpClient): Home? {
        val homeDoc = document(client, "") ?: return null
        val episodes = latestEpisodes(client, homeDoc)
        val series = latestSeries(client, homeDoc)

        return if (series.isNotEmpty()) {
            Home(
                episodes = episodes.toImmutableSet(),
                series = series.toImmutableSet()
            )
        } else {
            null
        }
    }

    private suspend fun cover(client: HttpClient, url: String): String? {
        val coverDoc = document(
            client,
            BSUtil.commonSeriesHref(url)
        ) ?: document(
            client,
            BSUtil.fixSeriesHref(url)
        ) ?: document(
            client,
            url
        )

        return coverDoc?.let { cover(it) }
    }

    private suspend fun cover(document: Document): String? {
        val seriesElement = document.firstClass("serie")
        val allImages = seriesElement?.getElementsByTag("img")

        val cover = (allImages?.firstOrNull {
            it.attr("alt").equals("Cover", ignoreCase = true)
        } ?: allImages?.firstOrNull())?.src()

        return cover?.let(BSUtil::getBurningSeriesLink)
    }
}