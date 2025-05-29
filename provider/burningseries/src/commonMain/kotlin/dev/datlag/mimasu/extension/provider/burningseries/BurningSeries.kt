package dev.datlag.mimasu.extension.provider.burningseries

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.mimasu.extension.ksoup.allByClass
import dev.datlag.mimasu.extension.ksoup.allByTag
import dev.datlag.mimasu.extension.ksoup.firstByTag
import dev.datlag.mimasu.extension.ksoup.href
import dev.datlag.mimasu.extension.ksoup.parseGet
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.getValue
import kotlin.time.Duration.Companion.hours

data object BurningSeries {

    private const val PROTOCOL_HTTPS = "https://"
    private const val HOST = "bs.to"
    private const val SEARCH_PATH = "andere-serien"

    internal val currentYear by lazy {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }

    private var searchItemsCacheTime = 0L
    private var cachedSearchItems = setOf<SearchItem>()
        get() {
            if (searchItemsCacheTime <= 0L || Clock.System.now().minus(12.hours).epochSeconds > searchItemsCacheTime) {
                return emptySet()
            }
            return field
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value.also {
                    if (it.isNotEmpty()) {
                        searchItemsCacheTime = Clock.System.now().epochSeconds
                    }
                }
            }
        }

    internal fun createLink(href: String): String {
        return if (!href.matches("^\\w+?://.*".toRegex())) {
            if (!href.startsWith('/')) {
                "$PROTOCOL_HTTPS$HOST/$href"
            } else {
                "$PROTOCOL_HTTPS$HOST${"(?!:|/{2,})(/.*)".toRegex().find(href)?.value}"
            }
        } else {
            href
        }
    }

    private fun normalize(href: String): String {
        val regex = "serie\\S+".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(href)?.value ?: href
    }

    internal suspend fun document(client: HttpClient, href: String): Document? = suspendCatching {
        Ksoup.parseGet(
            url = createLink(href),
            client = client
        )
    }.getOrNull()

    internal suspend fun search(client: HttpClient): Set<SearchItem> {
        cachedSearchItems.also {
            if (it.isNotEmpty()) {
                return it
            }
        }

        val doc = document(client, SEARCH_PATH) ?: return emptySet()

        return doc.getElementById("seriesContainer")?.allByClass("genre")?.map { element ->
            val genre = element.firstByTag("strong")?.text()?.ifBlank { null }?.trim()

            element.allByTag("li").mapNotNull { li ->
                val linkElement = li.firstByTag("a")
                val title = linkElement?.text()?.ifBlank { null }?.trim()
                val href = linkElement?.href()?.ifBlank { null }?.trim()?.let(::normalize)?.trim()

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
        }?.flatten()?.toSet()?.also {
            cachedSearchItems = it
        } ?: emptySet()
    }

}