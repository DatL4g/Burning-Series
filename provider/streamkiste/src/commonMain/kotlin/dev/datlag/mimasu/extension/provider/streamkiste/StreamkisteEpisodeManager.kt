package dev.datlag.mimasu.extension.provider.streamkiste

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.kache.CachePool
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.provider.streamkiste.model.Browse
import dev.datlag.mimasu.extension.provider.streamkiste.model.Season
import dev.datlag.mimasu.extension.provider.streamkiste.model.Watch
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class StreamkisteEpisodeManager(
    private val streamkiste: Streamkiste,
    private val fallbackStreamkiste: Streamkiste?,
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient,
) : CachePool {

    private val seasonKache = InMemoryKache<String, Set<Season>>(
        maxSize = 5L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
    }

    private val watchKache = InMemoryKache<String, Watch>(
        maxSize = 5L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
    }

    private val streamKache = InMemoryKache<String, Collection<String>>(
        maxSize = 2L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 10.minutes
    }

    override suspend fun clear(): Boolean {
        return suspendCatching {
            seasonKache.clear()
        }.isSuccess && suspendCatching {
            watchKache.clear()
        }.isSuccess && suspendCatching {
            streamKache.clear()
        }.isSuccess
    }

    suspend fun episodeAvailable(
        show: Browse.Item,
        episodeNumber: Int?,
        season: Int?
    ): Boolean {
        val watchData = watchData(
            id = show.id,
            title = show.title ?: "",
            season = season
        ) ?: return false

        return watchData.streams.any {
            it.episode == episodeNumber
        }
    }

    suspend fun episodeStreams(
        show: Browse.Item,
        episodeNumber: Int?,
        season: Int?
    ): List<String> = coroutineScope {
        val watchData = watchData(
            id = show.id,
            title = show.title ?: "",
            season = season
        ) ?: return@coroutineScope emptyList()

        val episodes = watchData.streams.filter {
            it.episode == episodeNumber
        }.ifEmpty { null } ?: return@coroutineScope emptyList()

        return@coroutineScope streams(episodes.map { it.stream }).toList()
    }

    private suspend fun watchData(id: String, title: String, season: Int?): Watch? {
        return if (season == null) {
            watchData(id)
        } else {
            val seasons = seasons(title) ?: return watchData(id)?.let {
                if (it.season == season) {
                    it
                } else {
                    null
                }
            }
            val seasonId = seasons.firstNotNullOfOrNull {
                if (it.season == season) {
                    it
                } else {
                    null
                }
            } ?: return watchData(id)?.let {
                if (it.season == season) {
                    it
                } else {
                    null
                }
            }

            watchData(seasonId.id)
        }
    }

    private suspend fun watchData(reqId: String): Watch? {
        return watchKache.getOrPut(reqId) {
            suspendCatching {
                streamkiste.watch(reqId)
            }.getOrNull() ?: suspendCatching {
                fallbackStreamkiste?.watch(reqId)
            }.getOrNull()
        }
    }

    private suspend fun seasons(title: String): Set<Season>? {
        val key = title.substringBeforeLast('-').trim()

        return seasonKache.getOrPut(key) {
            suspendCatching {
                streamkiste.seasons(Streamkiste.LANG_DE, key)
            }.getOrNull()?.ifEmpty { null } ?: suspendCatching {
                fallbackStreamkiste?.seasons(Streamkiste.LANG_DE, key)
            }.getOrNull()?.ifEmpty { null }
        }?.ifEmpty { null }
    }

    private suspend fun streams(urls: Collection<String>) = coroutineScope {
        val directLinks = urls.map { url -> async {
            streamKache.async(url) {
                suspendCatching {
                    Skeo.resolveStreams(url, httpClient)
                }.getOrNull() ?: suspendCatching {
                    Skeo.resolveStreams(url, fallbackClient)
                }.getOrNull()
            }?.toSet()
        } }.awaitAll().filterNotNull().flatten().toSet()
        val items = Skeo.filterNotSample(directLinks)
        val reachableLinks = Skeo.filterReachable(items, httpClient)

        reachableLinks.map { it }.toSet()
    }
}