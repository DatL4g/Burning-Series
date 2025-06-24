package dev.datlag.mimasu.extension.provider.serienstream.model

import co.touchlab.kermit.Logger
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.mimasu.extension.ksoup.allByClass
import dev.datlag.mimasu.extension.ksoup.allByTag
import dev.datlag.mimasu.extension.ksoup.firstByClass
import dev.datlag.mimasu.extension.ksoup.firstByTag
import dev.datlag.mimasu.extension.ksoup.href
import dev.datlag.mimasu.extension.ksoup.parseGet
import dev.datlag.mimasu.extension.ksoup.title
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient

data class Series(
    val baseUrl: String,
    val slug: String,
    override val info: SeriesData.Info,
    val seasons: Collection<Int>,
    val episodes: Collection<Episode>
) : SeriesData {

    val nextSeason = season?.let { current ->
        if (current <= 0) {
            return@let null
        }

        if (seasons.contains(current + 1)) {
            current + 1
        } else {
            null
        }
    }

    data class Episode(
        val number: Int,
        val title: String,
        val englishTitle: String?,
        val slug: String
    ) {

        suspend fun availableLanguages(document: Document): Collection<Language> {
            val languageBox = document.firstByClass("changeLanguageBox")
                ?: document.firstByClass("changeLanguage")
                ?: return emptyList()

            return languageBox.allByTag("img").mapNotNull { img ->
                val key = img.attr("data-lang-key").trim().ifBlank { null }?.toIntOrNull() ?: return@mapNotNull null
                val title = img.title()?.trim()?.ifBlank { null } ?: return@mapNotNull null

                Language(
                    key = key,
                    title = title
                )
            }.toSet()
        }

        suspend fun providers(document: Document): Collection<Provider> {
            val allEntries = document.firstByClass("hosterSiteVideo")?.allByTag("li")?.ifEmpty { null }
                ?: document.allByTag("li").ifEmpty { null } ?: return emptyList()

            return allEntries.mapNotNull { li ->
                val langKey = li.attr("data-lang-key").trim().ifBlank { null }?.toIntOrNull() ?: return@mapNotNull null
                val link = li.firstByClass("watchEpisode")?.href()?.trim()?.ifBlank { null }
                    ?: li.firstByTag("a")?.href()?.trim()?.ifBlank { null }
                    ?: li.attr("data-link-target").trim().ifBlank { null }
                    ?: li.attr("data-link-id").trim().ifBlank { null }?.let { "/redirect/$it" }
                    ?: return@mapNotNull null

                Provider(
                    languageKey = langKey,
                    redirectSlug = link
                )
            }.toSet()
        }

        data class Language(
            val key: Int,
            val title: String
        )

        data class Provider(
            val languageKey: Int,
            val redirectSlug: String
        )
    }

    companion object {
        suspend fun from(searchItem: SearchItem, slug: String, client: HttpClient): Series? {
            val doc = suspendCatching {
                Ksoup.parseGet(
                    url = searchItem.createLink(slug),
                    client = client
                )
            }.getOrNull() ?: return null

            val streamDiv = doc.getElementById("stream") ?: doc.firstByClass("hosterSiteDirectNav")
            val seasons = streamDiv?.allByTag("ul")
            val seasonLinks = seasons?.flatMap { ul ->
                ul.allByTag("a").mapNotNull { a ->
                    a.href()?.trim()?.ifBlank { null }
                }
            }?.toSet()?.mapNotNull { searchItem.seasonFrom(it) }?.toSet().orEmpty()

            val episodeTable = doc.selectFirst("table.seasonEpisodesList")
            val episodeTableBody = episodeTable?.firstByTag("tbody")
            val episodeEntries = episodeTableBody?.allByTag("tr")?.mapIndexedNotNull { index, tr ->
                val episodeNumber = tr.selectFirst("meta[itemprop=episodeNumber]")?.attr("content")?.trim()?.toIntOrNull()
                val titleElement = tr.firstByClass("seasonEpisodeTitle") ?: return@mapIndexedNotNull null
                val link = titleElement.firstByTag("a")?.href() ?: tr.firstByTag("a")?.href() ?: return@mapIndexedNotNull null
                val title = titleElement.firstByTag("strong")?.text()?.trim()?.ifBlank { null } ?: titleElement.text().trim().ifBlank { null } ?: return@mapIndexedNotNull null
                val englishTitle = titleElement.firstByTag("span")?.text()?.trim()?.ifBlank { null }

                Episode(
                    number = episodeNumber ?: searchItem.episodeFrom(link) ?: (index + 1),
                    title = title,
                    englishTitle = englishTitle,
                    slug = searchItem.normalize(link)
                )
            }.orEmpty()
            val locationSlug = doc.location()?.ifBlank { null }?.let(searchItem::normalize) ?: searchItem.normalize(slug)

            return Series(
                baseUrl = searchItem.baseUrl,
                slug = locationSlug,
                info = SeriesData.fromSearchItem(searchItem, searchItem.slugInfoStart(locationSlug)),
                seasons = seasonLinks,
                episodes = episodeEntries
            )
        }
    }
}
