package dev.datlag.burningseries.network.scraper

import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.common.getDigitsOrNull
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.common.getHref
import dev.datlag.burningseries.network.common.getSrc
import dev.datlag.burningseries.network.common.getTitle
import dev.datlag.burningseries.network.common.getValue
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

    suspend fun getHome(client: HttpClient): Home? {
        val doc = getDocument(client, String()) ?: return null
        return getHome(client, doc)
    }

    suspend fun getSeries(client: HttpClient, href: String): Series? {
        val docHref = BSUtil.fixSeriesHref(href)
        val doc = getDocument(client, docHref) ?: return null

        val titleElement = doc.querySelector(".serie")?.querySelector("h2") ?: return null
        val titleSeason = titleElement.querySelector("small")?.textContent()?.trim()
        val title = titleElement.textContent().replace(titleSeason ?: String(), String()).trim()
        val description = doc.querySelector(".serie")?.querySelector("#sp_left > p")?.textContent() ?: String()

        val seasons = doc.querySelector(".serie")?.querySelector("#seasons")?.querySelector("ul")?.querySelectorAll("li")?.mapIndexed { index, it ->
            val seasonTitle = it.querySelector("a")?.textContent() ?: it.textContent()
            val link = BSUtil.normalizeHref(it.querySelector("a")?.getHref() ?: it.getHref() ?: String())
            val value = if (link.isBlank()) {
                index
            } else {
                if (link.split('/').size >= 3) {
                    link.split('/')[2].toIntOrNull() ?: index
                } else {
                    index
                }
            }
            Series.Season(
                value = value,
                title = seasonTitle
            )
        } ?: emptyList()

        val selectedLanguageValue = doc.querySelector(".series-language")?.querySelector("option[selected]")?.getValue()
        var selectedLanguage: String? = null
        val languageElements = doc.querySelector(".series-language")?.querySelectorAll("option") ?: emptyList()

        val languages = languageElements.mapNotNull {
            val value = it.getValue() ?: String()
            val text = it.textContent()
            val selected = it.querySelector("option[selected]")?.getValue()
            if (!selected.isNullOrBlank() || (!selectedLanguageValue.isNullOrBlank() && selectedLanguageValue == value)) {
                selectedLanguage = value
            }
            if (value.isNotBlank() && text.isNotBlank()) {
                Series.Language(
                    value = value,
                    title = text
                )
            } else {
                null
            }
        }

        if (selectedLanguage.isNullOrBlank()) {
            selectedLanguage = selectedLanguageValue
            if (selectedLanguage.isNullOrBlank()) {
                selectedLanguage = languages.firstOrNull()?.value
            }
        }

        val episodesDoc = doc.querySelector(".serie")?.querySelector(".episodes")?.querySelectorAll("tr") ?: emptyList()
        val episodeInfoList = episodesDoc.mapNotNull { episodesElement ->
            val episodeList = episodesElement.querySelectorAll("td").mapNotNull { it.querySelector("a") }.map { data ->
                val text = data.querySelector("a")?.textContent() ?: String()
                val episodeHref = BSUtil.normalizeHref(data.querySelector("a")?.getHref() ?: String())

                text.trim() to episodeHref
            }

            val episodeHref = when {
                episodeList.isEmpty() -> return@mapNotNull null
                episodeList[0].second.isNotBlank() -> episodeList[0].second
                episodeList.size > 1 && episodeList[1].second.isNotBlank() -> episodeList[1].second
                else -> String()
            }

            val episodeTitle = if (episodeList.size > 1) episodeList[1].first.trim() else String()

            val hoster = episodeList.map { it.second }.filterNot { it.isBlank() }.toMutableSet()
            if (hoster.contains(episodeHref)) {
                hoster.remove(episodeHref)
            } else if (hoster.contains(episodeHref.trim())) {
                hoster.remove(episodeHref.trim())
            }

            val hosterList = hoster.map {
                Series.Episode.Hoster(
                    it.replace(episodeHref, String()).replace("/", ""),
                    it
                )
            }

            Series.Episode(
                number = episodeList[0].first.trim(),
                title = episodeTitle,
                href = episodeHref,
                hosters = hosterList
            )
        }

        val (cover, isNsfw) = getCover(doc)

        return Series(
            title = title,
            description = description,
            coverHref = cover,
            href = docHref,
            seasonTitle = titleSeason ?: String(),
            seasons = seasons,
            selectedLanguage = selectedLanguage?.trim(),
            languages = languages,
            episodes = episodeInfoList
        )
    }

    suspend fun getSearch(client: HttpClient): List<Genre> {
        val doc = getDocument(client, BSUtil.SEARCH) ?: return emptyList()
        return doc.getElementById("seriesContainer")?.querySelectorAll(".genre")?.mapNotNull { element ->
            val genre = element.querySelector("strong")?.textContent()?.trim() ?: String()

            if (genre.isNotBlank()) {
                Genre(
                    title = genre,
                    items = element.querySelectorAll("li").mapNotNull { item ->
                        val title = item.querySelector("a")?.textContent()?.trim() ?: String()
                        val href = BSUtil.normalizeHref(item.querySelector("a")?.getHref()?.trim() ?: String())

                        if (title.isNotBlank() && href.isNotBlank()) {
                            Genre.Item(
                                title = title.trim(),
                                href = href.trim()
                            )
                        } else {
                            null
                        }
                    }
                )
            } else {
                null
            }
        } ?: emptyList()
    }

    private suspend fun getCover(client: HttpClient, href: String): Pair<String?, Boolean> {
        return getCover(getDocument(client, BSUtil.commonSeriesHref(href)))
    }

    private suspend fun getCover(document: KtSoupDocument?): Pair<String?, Boolean> {
        val allImages = document?.querySelector(".serie")?.querySelectorAll("img")

        val cover = (allImages?.firstOrNull {
            it.attr("alt").equals("Cover", true)
        } ?: allImages?.firstOrNull())?.getSrc()

        val isNsfw = allImages?.firstOrNull {
            it.attr("alt").equals("AB 18", true)
        } != null

        return cover to isNsfw
    }
}