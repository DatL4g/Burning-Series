package dev.datlag.mimasu.extension.provider.streamkiste

import dev.datlag.mimasu.extension.kache.CachePool
import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.matcher.SearchMatcher
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.provider.streamkiste.model.Browse
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.setFrom
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StreamkisteSearchManager(
    private val streamkiste: Streamkiste,
    private val fallbackStreamkiste: Streamkiste?,
) : CachePool {

    private val seriesMappings = mutableMapOf<Int, MatchResult<Browse.Item>>()
    private val movieMappings = mutableMapOf<Int, MatchResult<Browse.Item>>()

    override suspend fun clear(): Boolean {
        return suspendCatching {
            seriesMappings.clear()
        }.isSuccess && suspendCatching {
            movieMappings.clear()
        }.isSuccess
    }

    suspend fun searchSeries(
        tmdbId: Int?,
        titles: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ): MatchResult<Browse.Item>? {
        seriesMappings[tmdbId]?.let {
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
            isAnimation = isAnimation,
            type = Type.TV
        )?.also { item ->
            tmdbId?.let {
                seriesMappings[it] = item
            }
        }
    }

    suspend fun searchMovie(
        tmdbId: Int?,
        titles: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ): MatchResult<Browse.Item>? {
        movieMappings[tmdbId]?.let {
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
            isAnimation = isAnimation,
            type = Type.Movie
        )?.also { item ->
            tmdbId?.let {
                movieMappings[it] = item
            }
        }
    }

    private suspend fun search(
        allQueries: Collection<String>,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?,
        type: Type
    ) = coroutineScope {
        val allFound = allQueries.map { query -> async {
            browse(query, type)
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

        val genreBased = when (isAnimation) {
            true -> {
                search(
                    tokens = tokens,
                    releaseYear = releaseYear,
                    filterItems = allFound.filter { it.isAnimation }
                )
            }
            false -> {
                search(
                    tokens = tokens,
                    releaseYear = releaseYear,
                    filterItems = allFound.filter { !it.isAnimation }
                )
            }
            else -> null
        }

        return@coroutineScope genreBased ?: search(
            tokens = tokens,
            releaseYear = releaseYear,
            filterItems = allFound
        )
    }

    private suspend fun search(
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        filterItems: Collection<Browse.Item>
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

    private suspend fun browse(
        query: String,
        type: Type
    ): Set<Browse.Item> = coroutineScope {
        val encodedSearch = async {
            suspendCatching {
                streamkiste.browseEncoded(
                    lang = Streamkiste.LANG_ALL,
                    keyword = query,
                    type = type.name
                )
            }.getOrNull()?.all.orEmpty().ifEmpty {
                suspendCatching {
                    fallbackStreamkiste?.browseEncoded(
                        lang = Streamkiste.LANG_ALL,
                        keyword = query,
                        type = type.name
                    )
                }.getOrNull()?.all.orEmpty()
            }
        }
        val plainSearch = async {
            suspendCatching {
                streamkiste.browsePlain(
                    lang = Streamkiste.LANG_ALL,
                    keyword = query,
                    type = type.name
                )
            }.getOrNull()?.all.orEmpty().ifEmpty {
                suspendCatching {
                    fallbackStreamkiste?.browsePlain(
                        lang = Streamkiste.LANG_ALL,
                        keyword = query,
                        type = type.name
                    )
                }.getOrNull()?.all.orEmpty()
            }
        }

        return@coroutineScope listOf(
            encodedSearch,
            plainSearch
        ).awaitAll().flatten().toSet()
    }
}