package dev.datlag.burningseries.network.scraper

import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.common.getHref
import dev.datlag.burningseries.network.common.getSrc
import dev.datlag.burningseries.network.common.getTitle
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ktsoup.KtSoupDocument
import ktsoup.KtSoupParser
import ktsoup.parseRemote
import ktsoup.setClient

data object BurningSeries {

    private suspend fun getDocument(client: HttpClient, url: String): KtSoupDocument? = suspendCatching {
        KtSoupParser.setClient(client)

        return@suspendCatching suspendCatching {
            KtSoupParser.parseRemote(BSUtil.getBurningSeriesLink(url))
        }.getOrNull() ?: suspendCatching {
            KtSoupParser.parseRemote(BSUtil.getBurningSeriesLink(url, true))
        }.getOrNull()
    }.getOrNull()

    private suspend fun getLatestEpisodes(client: HttpClient, document: KtSoupDocument) = coroutineScope {
        document.getElementById("newest_episodes")?.querySelectorAll("li")?.map {
            async {
                val episodeTitle = it.querySelector("li")?.querySelector("a")?.getTitle() ?: String()
                val episodeHref = BSUtil.normalizeHref(it.querySelector("li")?.querySelector("a")?.getHref() ?: String())
                val episodeInfo = it.querySelector("li")?.querySelector(".info")?.textContent() ?: String()
                val episodeFlagElements = it.querySelector("li")?.querySelector(".info")?.querySelectorAll("i")

                val episodeInfoFlags: MutableList<Home.Episode.Flag> = mutableListOf()
                episodeFlagElements?.forEach { infoFlags ->
                    val flagClass = infoFlags.querySelector("i")?.className() ?: String()
                    val flagTitle = infoFlags.querySelector("i")?.getTitle() ?: String()
                    episodeInfoFlags.add(
                        Home.Episode.Flag(
                            clazz = flagClass,
                            title = flagTitle
                        )
                    )
                }

                if (episodeTitle.isNotEmpty() && episodeHref.isNotEmpty()) {
                    val (cover, isNsfw) = getCover(client, episodeHref)

                    Home.Episode(
                        title = episodeTitle,
                        href = episodeHref,
                        info = episodeInfo,
                        flags = episodeInfoFlags,
                        coverHref = cover,
                        isNsfw = isNsfw
                    )
                } else {
                    null
                }
            }
        }?.awaitAll()?.filterNotNull() ?: emptyList()
    }

    private suspend fun getLatestSeries(client: HttpClient, document: KtSoupDocument) = coroutineScope {
        document.getElementById("newest_series")?.querySelectorAll("li")?.map {
            async {
                val seriesTitle = it.querySelector("a")?.getTitle() ?: String()
                val seriesHref = BSUtil.normalizeHref(it.querySelector("a")?.getHref() ?: String())

                if (seriesTitle.isNotEmpty() && seriesHref.isNotEmpty()) {
                    val (cover, isNsfw) = getCover(client, seriesHref)

                    Home.Series(
                        title = seriesTitle,
                        href = seriesHref,
                        isNsfw = isNsfw,
                        coverHref = cover
                    )
                } else {
                    null
                }
            }
        }?.awaitAll()?.filterNotNull() ?: emptyList()
    }

    private suspend fun getHome(client: HttpClient, document: KtSoupDocument): Home? {
        val episodes = getLatestEpisodes(client, document)
        val series = getLatestSeries(client, document)

        return if (episodes.isNotEmpty() && series.isNotEmpty()) {
            Home(
                episodes = episodes,
                series = series
            )
        } else {
            null
        }
    }

    private suspend fun getCover(client: HttpClient, href: String): Pair<String, Boolean> {
        return getCover(getDocument(client, href))
    }

    private suspend fun getCover(document: KtSoupDocument?): Pair<String, Boolean> {
        val allImages = document?.querySelector(".serie")?.querySelectorAll("img")

        val cover = (allImages?.firstOrNull {
            it.attr("alt").equals("Cover", true)
        } ?: allImages?.firstOrNull())?.getSrc()

        val isNsfw = allImages?.firstOrNull {
            it.attr("alt").equals("AB 18", true)
        } != null

        return (cover ?: String()) to isNsfw
    }

    suspend fun testSeries(client: HttpClient) {
        val doc = getDocument(client, String())

        doc?.let {
            getHome(client, it)?.let(::println)
        }

        doc?.close()
    }
}