package dev.datlag.mimasu.extension.provider.serienstream

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.matcher.SearchMatcher
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchResult
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.setFrom
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CombinedSearchManager(
    private val serienStream: SerienStream,
    private val fallbackSerienStream: SerienStream?,
    private val aniWorld: AniWorld,
    private val fallbackAniWorld: AniWorld?
) {

    private val mappings = mutableMapOf<Int, MatchResult<SearchResult>>()

    suspend fun search(
        tmdbId: Int?,
        titles: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ): MatchResult<SearchResult>? {
        mappings[tmdbId]?.let {
            return it
        }

        val tokenQueries = tokens.mapNotNull {
            listOf(it.tokens, it.extraTokens).flatten().sortedBy { t ->
                t.index
            }.joinToString(separator = " ", truncated = "") { t -> t.value }.trim().ifBlank { null }
        }.toSet()
        val allQueries = setFrom(tokenQueries, titles).toSet()

        return search(
            allQueries = allQueries,
            tokens = tokens,
            releaseYear = releaseYear,
            isAnimation = isAnimation
        )?.also { item ->
            tmdbId?.let {
                mappings[it] = item
            }
        }
    }

    private suspend fun search(
        allQueries: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ) = coroutineScope {
        val allFound = allQueries.map { query -> async {
            if (isAnimation == true) {
                searchAnimation(query)
            } else {
                searchDefault(query)
            }
        } }.awaitAll().flatten().toSet()

        allFound.singleOrNull()?.let {
            val similarity = when {
                releaseYear != null && it.releaseYear != null && releaseYear == it.releaseYear -> 1.15
                releaseYear != null && it.releaseYear != null && releaseYear != it.releaseYear -> 0.51
                else -> 1.0
            }

            return@coroutineScope MatchResult(
                similarity = similarity,
                data = it
            )
        }

        return@coroutineScope search(
            tokens = tokens,
            releaseYear = releaseYear,
            filterItems = allFound
        )
    }

    private suspend fun search(
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        filterItems: Collection<SearchResult>
    ) = coroutineScope {
        val matched = filterItems.map { item -> async {
            val itemTokenList = setOfNotNull(
                item.tokenResult,
                item.alternativeTokenResult
            )
            val similarity = tokens.map { tokens -> async {
                itemTokenList.maxOfOrNull { searchToken ->
                    SearchMatcher.calculateSymmetricSimilarity(searchToken, tokens)
                }
            } }.awaitAll().filterNotNull().max()

            MatchResult(
                similarity = similarity,
                data = item
            )
        } }.awaitAll().filter {
            it.similarity > 0.1
        }.map { (score, item) ->
            if (releaseYear != null && item.releaseYear != null && releaseYear == item.releaseYear) {
                return@map MatchResult(
                    similarity = score + 0.15,
                    data = item
                )
            }

            MatchResult(
                similarity = score,
                data = item
            )
        }.filter {
            it.similarity > 0.5
        }

        val found = matched.maxByOrNull { it.similarity }

        return@coroutineScope found
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