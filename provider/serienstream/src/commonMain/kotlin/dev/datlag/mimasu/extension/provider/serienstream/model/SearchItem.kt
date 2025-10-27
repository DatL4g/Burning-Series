package dev.datlag.mimasu.extension.provider.serienstream.model

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.mimasu.extension.kache.CachePool
import dev.datlag.mimasu.extension.ksoup.allByTag
import dev.datlag.mimasu.extension.ksoup.firstByTag
import dev.datlag.mimasu.extension.ksoup.href
import dev.datlag.mimasu.extension.ksoup.parseGet
import dev.datlag.mimasu.extension.ksoup.title
import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlinx.datetime.toLocalDateTime
import kotlin.getValue
import kotlin.time.Clock

sealed interface SearchItem : SeriesData, TokenAware {

    val name: String
    val slug: String
    val description: String?
    val cover: String?
    val productionYear: String?

    val releaseYear: Int?
    val alternativeTokenResult: TokenResult?

    val baseUrl: String
    val sourceTitle: String

    val localeCodes: Map<Int, String>

    fun normalize(slug: String = this.slug): String

    fun createLink(slug: String = normalize()) = createLink(baseUrl, slug)

    fun seasonFrom(slug: String): Int? {
        val matchResult = seasonRegex.find(slug) ?: run {
            if (slug.contains("/filme")) {
                return 0
            } else {
                null
            }
        }

        return matchResult?.groupValues?.lastOrNull()?.toIntOrNull()
            ?: matchResult?.groupValues?.firstNotNullOfOrNull {
                it.toIntOrNull()
            }
    }

    fun episodeFrom(slug: String): Int? {
        val normalized = normalize(slug)
        val matchResult = episodeRegex.find(normalized)

        return matchResult?.groupValues?.lastOrNull()?.toIntOrNull()
            ?: matchResult?.groupValues?.firstNotNullOfOrNull {
                it.toIntOrNull()
            }
    }

    fun slugInfoStart(slug: String = this.slug): String
    fun toSlug(newSeason: Int? = season) = normalize(info.toSlug(newSeason))

    @Serializable
    data class AniWorld(
        @SerialName("name") override val name: String,
        @SerialName("link") override val slug: String,
        @SerialName("description") override val description: String? = null,
        @SerialName("cover") override val cover: String? = null,
        @SerialName("productionYear") override val productionYear: String? = null
    ) : SearchItem {

        @Serializable
        override val info: SeriesData.Info = SeriesData.fromSearchItem(this)

        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(name)

        @Transient
        private val numbers = tokenResult.tokens.mapNotNull { token ->
            token.value.toIntOrNull()
        }.toSet()

        @Transient
        override val releaseYear: Int? = productionYear
            ?.split("-")
            ?.firstOrNull()
            ?.replace(productionSanitizeRegex, "")
            ?.trim()
            ?.toIntOrNull()
            ?: numbers.firstNotNullOfOrNull { token ->
                token.takeIf { it in 1000..currentYear }
            }

        @Transient
        override val alternativeTokenResult: TokenResult? = releaseYear?.let { year ->
            tokenResult.copy(
                tokens = tokenResult.tokens.filterNot { it.value == year.toString() }
            )
        }

        @Transient
        override val baseUrl: String = BASE_URL

        @Transient
        override val sourceTitle: String = SOURCE_TITLE

        @Transient
        override val localeCodes: Map<Int, String> = mapOf(
            1 to "de",
            2 to "ens",
            3 to "des"
        )

        override fun normalize(slug: String): String = Companion.normalize(slug)

        override fun slugInfoStart(slug: String): String {
            var startSlug = normalize(slug)
            if (startSlug.startsWith('/')) {
                startSlug = startSlug.substring(1)
            }
            if (startSlug.startsWith(SERIES_PREFIX, ignoreCase = true)) {
                startSlug = startSlug.substring(SERIES_PREFIX.length)
            }
            return if (startSlug.startsWith('/')) {
                startSlug.substring(1)
            } else {
                startSlug
            }
        }

        companion object : CachePool {
            const val BASE_URL = "https://aniworld.to/"
            private const val SERIES_PREFIX = "anime/stream"
            const val SOURCE_TITLE = "AniWorld"
            private const val SEARCH_HREF = "animes"

            private var indexedItemsCacheTime = 0L

            @OptIn(ExperimentalTime::class)
            private var indexedSearchItems = setOf<AniWorld>()
                get() {
                    if (indexedItemsCacheTime <= 0L || Clock.System.now().minus(12.hours).epochSeconds > indexedItemsCacheTime) {
                        return emptySet()
                    }
                    return field
                }
                set(value) {
                    if (value.isNotEmpty()) {
                        field = value.also {
                            if (it.isNotEmpty()) {
                                indexedItemsCacheTime = Clock.System.now().epochSeconds
                            }
                        }
                    }
                }

            private val searchIndexMutex = Mutex()

            override suspend fun clear(): Boolean {
                indexedItemsCacheTime = 0L

                return true
            }

            fun normalize(slug: String): String {
                val regex = "anime\\S+".toRegex(RegexOption.IGNORE_CASE)
                return regex.find(slug)?.value ?: run {
                    val path = if (slug.startsWith('/')) {
                        slug
                    } else {
                        "/$slug"
                    }

                    "$SERIES_PREFIX$path"
                }
            }

            suspend fun searchIndex(client: HttpClient): Set<AniWorld> {
                indexedSearchItems.also {
                    if (it.isNotEmpty()) {
                        return it
                    }
                }

                return atomicSearch(client)
            }

            private suspend fun atomicSearch(client: HttpClient): Set<AniWorld> = searchIndexMutex.withLock {
                indexedSearchItems.also {
                    if (it.isNotEmpty()) {
                        return it
                    }
                }

                val doc = document(
                    client = client,
                    baseUrl = BASE_URL,
                    href = SEARCH_HREF
                ) ?: return emptySet()

                return doc.getElementById("seriesContainer")?.allByTag("li")?.mapNotNull { li ->
                    val linkElement = li.firstByTag("a") ?: return@mapNotNull null

                    val title = linkElement.text().ifBlank { null }?.trim() ?: linkElement.title()?.ifBlank { null }?.trim()
                    val href = linkElement.href()?.ifBlank { null }?.trim()?.let(::normalize)?.trim()

                    if (!title.isNullOrBlank() && !href.isNullOrBlank()) {
                        AniWorld(
                            name = title,
                            slug = href,
                        )
                    } else {
                        null
                    }
                }?.toSet()?.also {
                    indexedSearchItems = it
                } ?: emptySet()
            }
        }
    }

    @Serializable
    data class SerienStream(
        @SerialName("name") override val name: String,
        @SerialName("link") override val slug: String,
        @SerialName("description") override val description: String? = null,
        @SerialName("cover") override val cover: String? = null,
        @SerialName("productionYear") override val productionYear: String? = null
    ) : SearchItem {

        @Serializable
        override val info: SeriesData.Info = SeriesData.fromSearchItem(this)

        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(name)

        @Transient
        private val numbers = tokenResult.tokens.mapNotNull { token ->
            token.value.toIntOrNull()
        }.toSet()

        @Transient
        override val releaseYear: Int? = productionYear
            ?.split("-")
            ?.firstOrNull()
            ?.replace(productionSanitizeRegex, "")
            ?.trim()
            ?.toIntOrNull()
            ?: numbers.firstNotNullOfOrNull { token ->
                token.takeIf { it in 1000..currentYear }
            }

        @Transient
        override val alternativeTokenResult: TokenResult? = releaseYear?.let { year ->
            tokenResult.copy(
                tokens = tokenResult.tokens.filterNot { it.value == year.toString() }
            )
        }

        @Transient
        override val baseUrl: String = BASE_URL

        @Transient
        override val sourceTitle: String = SOURCE_TITLE

        @Transient
        override val localeCodes: Map<Int, String> = mapOf(
            1 to "de",
            2 to "en",
            3 to "des"
        )

        override fun normalize(slug: String): String = Companion.normalize(slug)

        override fun slugInfoStart(slug: String): String {
            var startSlug = normalize(slug)
            if (startSlug.startsWith('/')) {
                startSlug = startSlug.substring(1)
            }
            if (startSlug.startsWith(SERIES_PREFIX, ignoreCase = true)) {
                startSlug = startSlug.substring(SERIES_PREFIX.length)
            }
            return if (startSlug.startsWith('/')) {
                startSlug.substring(1)
            } else {
                startSlug
            }
        }

        companion object : CachePool {
            const val BASE_URL = "https://s.to/"
            private const val SERIES_PREFIX = "serie/stream"
            const val SOURCE_TITLE = "SerienStream"
            private const val SEARCH_HREF = "serien"

            private var indexedItemsCacheTime = 0L

            @OptIn(ExperimentalTime::class)
            private var indexedSearchItems = setOf<SerienStream>()
                get() {
                    if (indexedItemsCacheTime <= 0L || Clock.System.now().minus(12.hours).epochSeconds > indexedItemsCacheTime) {
                        return emptySet()
                    }
                    return field
                }
                set(value) {
                    if (value.isNotEmpty()) {
                        field = value.also {
                            if (it.isNotEmpty()) {
                                indexedItemsCacheTime = Clock.System.now().epochSeconds
                            }
                        }
                    }
                }

            private val searchIndexMutex = Mutex()

            override suspend fun clear(): Boolean {
                indexedItemsCacheTime = 0L
                return true
            }

            fun normalize(slug: String): String {
                val regex = "serie\\S+".toRegex(RegexOption.IGNORE_CASE)
                return regex.find(slug)?.value ?: run {
                    val path = if (slug.startsWith('/')) {
                        slug
                    } else {
                        "/$slug"
                    }

                    "$SERIES_PREFIX$path"
                }
            }

            suspend fun searchIndex(client: HttpClient): Set<SerienStream> {
                indexedSearchItems.also {
                    if (it.isNotEmpty()) {
                        return it
                    }
                }

                return atomicSearch(client)
            }

            private suspend fun atomicSearch(client: HttpClient): Set<SerienStream> = searchIndexMutex.withLock {
                indexedSearchItems.also {
                    if (it.isNotEmpty()) {
                        return it
                    }
                }

                val doc = document(
                    client = client,
                    baseUrl = BASE_URL,
                    href = SEARCH_HREF
                ) ?: return emptySet()

                return doc.getElementById("seriesContainer")?.allByTag("li")?.mapNotNull { li ->
                    val linkElement = li.firstByTag("a") ?: return@mapNotNull null

                    val title = linkElement.text().ifBlank { null }?.trim() ?: linkElement.title()?.ifBlank { null }?.trim()
                    val href = linkElement.href()?.ifBlank { null }?.trim()?.let(::normalize)?.trim()

                    if (!title.isNullOrBlank() && !href.isNullOrBlank()) {
                        SerienStream(
                            name = title,
                            slug = href,
                        )
                    } else {
                        null
                    }
                }?.toSet()?.also {
                    indexedSearchItems = it
                } ?: emptySet()
            }
        }
    }

    companion object : CachePool {
        private val productionSanitizeRegex = "\\D".toRegex()
        private val seasonRegex = "(staffel|season)[-+]?(\\d+)".toRegex(RegexOption.IGNORE_CASE)
        private val episodeRegex = "(episode|folge|film)[-+]?(\\d+)".toRegex(RegexOption.IGNORE_CASE)

        @OptIn(ExperimentalTime::class)
        private val currentYear by lazy {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        }

        override suspend fun clear(): Boolean {
            return AniWorld.clear() && SerienStream.clear()
        }

        fun createLink(baseUrl: String, slug: String): String {
            return if (!slug.matches("^\\w+?://.*".toRegex())) {
                if (!slug.startsWith('/')) {
                    "$baseUrl$slug"
                } else {
                    val part = "(?!:|/{2,})(/.*)".toRegex().find(slug)?.value?.ifBlank { null } ?: slug
                    val path = if (part.startsWith('/')) {
                        part.substring(1)
                    } else {
                        part
                    }

                    "$baseUrl${path}"
                }
            } else {
                slug
            }
        }

        private suspend fun document(
            client: HttpClient,
            baseUrl: String,
            href: String
        ): Document? = suspendCatching {
            Ksoup.parseGet(
                url = createLink(baseUrl, href),
                client = client
            )
        }.getOrNull()

        internal fun useAniWorld(tmdbId: Int?, isAnimation: Boolean): Boolean {
            val animationsOffAniWorld = listOf(
                246, // Avatar
                33880, // Korra
                604, // Teen Titans
                45140, // Teen Titans Go
                2604, // Boondocks
                123548, // Castlevania: Nocturne
                89456, // Primal
                2723, // Samurai Jack
                98494, // Glitch Techs
                66558, // Voltron
                94605, // Arcane
            )

            return when (tmdbId) {
                in animationsOffAniWorld -> false
                else -> isAnimation
            }
        }
    }
}