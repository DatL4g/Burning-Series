package dev.datlag.mimasu.extension.provider.serienstream

import co.touchlab.kermit.Logger
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchResult
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.setFrom
import io.ktor.client.call.body
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CombinedSearchManager(
    private val serienStream: SerienStream,
    private val fallbackSerienStream: SerienStream?,
    private val aniWorld: AniWorld,
    private val fallbackAniWorld: AniWorld?
) {

    suspend fun search(
        tmdbId: Int?,
        titles: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ): Int? {
        Logger.e("Start searching for s.to and aniworld.to")

        val tokenQueries = tokens.mapNotNull {
            listOf(it.tokens, it.extraTokens).flatten().sortedBy { t ->
                t.index
            }.joinToString(separator = " ", truncated = "") { t -> t.value }.trim().ifBlank { null }
        }.toSet()
        val allQueries = setFrom(tokenQueries, titles).toSet()

        Logger.e("Got search queries: $allQueries")

        search(allQueries, isAnimation)

        return null
    }


    private suspend fun search(
        allQueries: Collection<String>,
        isAnimation: Boolean?
    ) = coroutineScope {
        val allFound = allQueries.map { query -> async {
            if (isAnimation == true) {
                searchAnimation(query)
            } else {
                searchDefault(query)
            }
        } }.awaitAll().flatten().toSet()

        Logger.e("SerienStream Found: ${allFound.singleOrNull()}")

        allFound.singleOrNull()?.let {
            return@coroutineScope it
        }

        Logger.e("SerienStream All Results: ${allFound.size}")
    }

    private suspend fun searchDefault(
        query: String
    ): Set<SearchResult> = coroutineScope {
        val encodedSearch = async {
            suspendCatching {
                serienStream.searchEncoded(query)
            }.getOrNull().orEmpty().ifEmpty { suspendCatching {
                fallbackSerienStream?.searchEncoded(query)
            }.getOrNull().orEmpty() }
        }
        val plainSearch = async {
            suspendCatching {
                serienStream.searchPlain(query)
            }.getOrNull().orEmpty().ifEmpty { suspendCatching {
                fallbackSerienStream?.searchPlain(query)
            }.getOrNull().orEmpty() }
        }

        return@coroutineScope listOf(
            encodedSearch,
            plainSearch
        ).awaitAll().flatten().toSet()
    }

    private suspend fun searchAnimation(
        query: String
    ): Set<SearchResult> = coroutineScope {
        val encodedSearch = async {
            suspendCatching {
                aniWorld.searchEncoded(query)
            }.getOrNull().orEmpty().ifEmpty { suspendCatching {
                fallbackAniWorld?.searchEncoded(query)
            }.getOrNull().orEmpty() }
        }
        val plainSearch = async {
            suspendCatching {
                aniWorld.searchPlain(query)
            }.getOrNull().orEmpty().ifEmpty { suspendCatching {
                fallbackAniWorld?.searchPlain(query)
            }.getOrNull().orEmpty() }
        }

        return@coroutineScope listOf(
            encodedSearch,
            plainSearch
        ).awaitAll().flatten().toSet()
    }
}