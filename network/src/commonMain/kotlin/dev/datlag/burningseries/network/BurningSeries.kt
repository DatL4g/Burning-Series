package dev.datlag.burningseries.network

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.common.allClass
import dev.datlag.burningseries.network.common.allTag
import dev.datlag.burningseries.network.common.firstClass
import dev.datlag.burningseries.network.common.firstTag
import dev.datlag.burningseries.network.common.href
import dev.datlag.burningseries.network.common.parseGet
import dev.datlag.burningseries.network.common.src
import dev.datlag.burningseries.network.common.title
import dev.datlag.tooling.async.suspendCatching
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
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
                    Home.Episode(
                        fullTitle = episodeTitle,
                        href = episodeHref,
                        info = episodeInfo?.ifBlank { null },
                        flags = episodeInfoFlags.toImmutableSet(),
                        coverHref = cover(client, episodeHref)
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

                    Home.Series(
                        title = seriesTitle,
                        href = seriesHref,
                        coverHref = cover(client, seriesHref)
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

    internal suspend fun search(client: HttpClient): ImmutableSet<SearchItem> {
        val doc = document(client, BSUtil.SEARCH) ?: return persistentSetOf()

        return doc.getElementById("seriesContainer")?.allClass("genre")?.map { element ->
            val genre = element.firstTag("strong")?.text()?.ifBlank { null }

            element.getElementsByTag("li").mapNotNull { li ->
                val linkElement = li.firstTag("a")
                val title = linkElement?.text()?.ifBlank { null }
                val href = linkElement?.href()?.ifBlank { null }?.let(BSUtil::fixSeriesHref)

                if (!title.isNullOrBlank() && !href.isNullOrBlank()) {
                    SearchItem(
                        title = title,
                        href = href,
                        genre = genre
                    )
                } else {
                    null
                }
            }
        }?.flatten()?.toImmutableSet() ?: persistentSetOf()
    }

    internal suspend fun series(client: HttpClient, href: String): Series? {
        val doc = document(client, BSUtil.fixSeriesHref(href)) ?: return null

        val titleElement = doc.firstClass("serie")?.firstTag("h2") ?: return null
        val titleSeason = titleElement.firstTag("small")?.text()?.trim()
        val title = titleElement.text().replace(titleSeason ?: "", "").trim()
        val description = doc.firstClass("serie")?.selectFirst("#sp_left > p")?.text()

        val seasons = doc.firstClass("serie")
            ?.getElementById("seasons")
            ?.firstTag("ul")
            ?.allTag("li")
            ?.mapIndexed { index, element ->
                val seasonTitle = element.firstTag("a")?.text()?.ifBlank { null } ?: element.text()
                val link = (element.firstTag("a")?.href() ?: element.href())?.let(BSUtil::fixSeriesHref)
                val value = if (link.isNullOrBlank()) {
                    index
                } else {
                    link.let(BSUtil::seasonFrom) ?: index
                }

                Series.Season(
                    value = value,
                    title = seasonTitle
                )
        }?.toImmutableSet() ?: persistentSetOf()

        val selectedLanguageValue = doc.firstClass("series-language")?.selectFirst("option[selected]")?.value()?.ifBlank { null }
        var selectedLanguage: String? = null
        val languageElements = doc.firstClass("series-language")?.select("option").orEmpty()

        val languages = languageElements.mapNotNull {
            val value = it.value().ifBlank { null }
            val text = it.text()
            val selected = it.selectFirst("option[selected]")?.value()

            if (!selected.isNullOrBlank() || (!selectedLanguageValue.isNullOrBlank() && selectedLanguageValue == value)) {
                selectedLanguage = value
            }
            if (!value.isNullOrBlank() && text.isNotBlank()) {
                Series.Language(
                    value = value,
                    title = text
                )
            } else {
                null
            }
        }.toImmutableSet()

        if (selectedLanguage.isNullOrBlank()) {
            selectedLanguage = selectedLanguageValue
            if (selectedLanguage.isNullOrBlank()) {
                selectedLanguage = languages.firstOrNull()?.value
            }
        }

        val infoElement = doc.firstClass("serie")?.firstClass("infos")
        val infoHeader = infoElement?.select("div > span").orEmpty().map {
            it.text().trim()
        }
        val infoData = infoElement?.allTag("p").orEmpty().map {
            it.text().trim()
        }.toMutableList().apply {
            remove(description)
            remove(description?.trim())
        }
        val infoList: MutableList<Series.Info> = mutableListOf()

        for (i in infoHeader.indices) {
            val data = if (infoData.size > i && infoData[i].isNotBlank()) {
                infoData[i].replace(Regex("(?:(\\n)*\\t)+", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)), "\t")
            } else {
                ""
            }

            infoList.add(
                Series.Info(
                    infoHeader[i],
                    data
                )
            )
        }

        val episodesElement = doc.firstClass("serie")?.firstClass("episodes")?.allTag("tr").orEmpty()
        val episodeInfoList = episodesElement.mapNotNull { element ->
            val episodeList = element.allTag("td").flatMap { it.allTag("a") }.map { data ->
                val text = data.text()
                val episodeHref = data.href()?.let(BSUtil::normalizeHref)

                text.trim() to episodeHref
            }

            val episodeHref = when {
                episodeList.isEmpty() -> return@mapNotNull null
                !episodeList[0].second.isNullOrBlank() -> episodeList[0].second
                episodeList.size > 1 && !episodeList[1].second.isNullOrBlank() -> episodeList[1].second
                else -> return@mapNotNull null
            } ?: return@mapNotNull null

            val episodeTitle = if (episodeList.size > 1) episodeList[1].first.trim() else ""

            val hoster = episodeList.map { it.second }.filterNot { it.isNullOrBlank() }.toMutableSet().apply {
                remove(episodeHref)
                remove(episodeHref.trim())
            }.filterNotNull()

            val hosterList = hoster.map {
                Series.Episode.Hoster(
                    title = it.replace(episodeHref, "").replace("/", ""),
                    href = it
                )
            }

            Series.Episode(
                number = episodeList[0].first.trim(),
                fullTitle = episodeTitle,
                href = episodeHref,
                hoster = hosterList.toImmutableSet()
            )
        }

        return Series(
            title = title,
            description = description ?: "",
            coverHref = cover(doc) ?: cover(client, href),
            href = doc.location()?.let(BSUtil::fixSeriesHref) ?: BSUtil.fixSeriesHref(href),
            seasonTitle = titleSeason ?: "",
            seasons = seasons,
            selectedLanguage = selectedLanguage?.trim(),
            languages = languages,
            info = infoList.toImmutableSet(),
            episodes = episodeInfoList.toImmutableSet()
        )
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