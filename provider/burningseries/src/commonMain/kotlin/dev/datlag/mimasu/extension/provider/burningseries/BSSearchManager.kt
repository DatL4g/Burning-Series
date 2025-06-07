package dev.datlag.mimasu.extension.provider.burningseries

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.matcher.SearchMatcher
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class BSSearchManager(
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient?,
) {

    private val burningSeriesMappings = mutableMapOf<Int, MatchResult<SearchItem>>()

    suspend fun initialize(): Set<SearchItem> {
        return BurningSeries.search(httpClient).ifEmpty {
            fallbackClient?.let { BurningSeries.search(fallbackClient) }
        }.orEmpty()
    }

    suspend fun search(
        tmdbId: Int?,
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        isAnimation: Boolean?
    ): MatchResult<SearchItem>? {
        burningSeriesMappings[tmdbId]?.let {
            return it
        }

        val searchItems = initialize().ifEmpty { null } ?: return null

        val (filteredSearchItems, otherSearchItems) = when (isAnimation) {
            true -> searchItems.filter {
                it.isAnimation == true
            } to searchItems.filterNot { it.isAnimation == true }
            false -> searchItems.filter {
                it.isAnimation == false
            } to searchItems.filterNot { it.isAnimation == false }
            else -> searchItems to emptyList()
        }

        val bestResult = search(
            tokens = tokens,
            releaseYear = releaseYear,
            filterItems = filteredSearchItems
        ) ?: search(
            tokens = tokens,
            releaseYear = releaseYear,
            filterItems = otherSearchItems
        )

        return bestResult?.also { item ->
            tmdbId?.let {
                burningSeriesMappings[it] = item
            }
        }
    }

    private suspend fun search(
        tokens: Collection<TokenResult>,
        releaseYear: Int?,
        filterItems: Collection<SearchItem>
    ) = coroutineScope {
        val matched = filterItems.map { item -> async {
            val itemTokenList = setOf(
                item.tokenResult,
                *item.alternativeTokenResults.toTypedArray()
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

}