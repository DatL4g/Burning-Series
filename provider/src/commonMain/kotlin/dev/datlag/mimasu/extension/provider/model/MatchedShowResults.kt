package dev.datlag.mimasu.extension.provider.model

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchResult
import kotlinx.serialization.Serializable

@Serializable
data class MatchedShowResults(
    val burningSeries: MatchResult<SearchItem>? = null,
    val serienStream: MatchResult<SearchResult>? = null
) {
    operator fun plus(other: MatchedShowResults): MatchedShowResults = this.copy(
        burningSeries = burningSeries ?: other.burningSeries,
        serienStream = serienStream ?: other.serienStream
    )

    fun isEmpty(): Boolean {
        return burningSeries == null && serienStream == null
    }
}