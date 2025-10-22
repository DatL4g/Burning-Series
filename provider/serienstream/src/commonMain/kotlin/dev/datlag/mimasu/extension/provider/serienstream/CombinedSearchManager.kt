package dev.datlag.mimasu.extension.provider.serienstream

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.matcher.SearchMatcher
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.setFrom
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CombinedSearchManager(
    private val serienStream: SerienStream,
    private val fallbackSerienStream: SerienStream?,
    private val aniWorld: AniWorld,
    private val fallbackAniWorld: AniWorld?,
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient?
) {

    private val mappings = mutableMapOf<Int, MatchResult<SearchItem>>()

    private suspend fun initializeAniWorld(): Set<SearchItem.AniWorld> {
        return SearchItem.AniWorld.searchIndex(httpClient).ifEmpty {
            fallbackClient?.let { SearchItem.AniWorld.searchIndex(it) }
        }.orEmpty()
    }

    private suspend fun initializeSerienStream(): Set<SearchItem.SerienStream> {
        return SearchItem.SerienStream.searchIndex(httpClient).ifEmpty {
            fallbackClient?.let { SearchItem.SerienStream.searchIndex(it) }
        }.orEmpty()
    }

    suspend fun initializeCombined(): Set<SearchItem> = coroutineScope {
        val aniWorld = async {
            initializeAniWorld()
        }

        val serienStream = async {
            initializeSerienStream()
        }

        return@coroutineScope setFrom(
            aniWorld.await(),
            serienStream.await()
        )
    }

    suspend fun search(
        tmdbId: Int?,
        titles: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ): MatchResult<SearchItem>? {
        mappings[tmdbId]?.let {
            return it
        }

        val tokenQueries = tokens.mapNotNull {
            listOf(it.tokens, it.extraTokens).flatten().sortedBy { t ->
                t.index
            }.joinToString(separator = " ", truncated = "") { t -> t.value }.trim().ifBlank { null }
        }.toSet()
        val allQueries = setFrom(tokenQueries, titles).toSet()
        val useAniWorld = SearchItem.useAniWorld(
            tmdbId = tmdbId,
            isAnimation = isAnimation == true
        )

        return search(
            allQueries = allQueries,
            tokens = tokens,
            releaseYear = releaseYear,
            useAniWorld = useAniWorld
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
        useAniWorld: Boolean
    ) = coroutineScope {
        val allFound = allQueries.map { query -> async {
            if (useAniWorld) {
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
        ) ?: if (useAniWorld) {
            searchAnimationFromIndex(
                tokens = tokens,
                releaseYear = releaseYear
            )
        } else {
            searchDefaultFromIndex(
                tokens = tokens,
                releaseYear = releaseYear
            )
        }
    }

    private suspend fun search(
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        filterItems: Collection<SearchItem>
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
    ): Set<SearchItem> = coroutineScope {
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
    ): Set<SearchItem> = coroutineScope {
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

    private suspend fun searchDefaultFromIndex(
        tokens: Collection<TokenResult>,
        releaseYear: Int?
    ): MatchResult<SearchItem>? {
        val searchIndex = initializeSerienStream().ifEmpty { null } ?: return null

        return search(
            tokens = tokens,
            releaseYear = releaseYear,
            filterItems = searchIndex
        )
    }

    private suspend fun searchAnimationFromIndex(
        tokens: Collection<TokenResult>,
        releaseYear: Int?
    ): MatchResult<SearchItem>? {
        val searchIndex = initializeAniWorld().ifEmpty { null } ?: return null

        return search(
            tokens = tokens,
            releaseYear = releaseYear,
            filterItems = searchIndex
        )
    }
}