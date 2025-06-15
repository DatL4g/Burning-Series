package dev.datlag.mimasu.extension.provider.model

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem as BSItem
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem as SAWItem
import kotlinx.serialization.Serializable

@Serializable
data class MatchedShowResults(
    val burningSeries: MatchResult<BSItem>? = null,
    val serienStream: MatchResult<SAWItem>? = null
) {
    operator fun plus(other: MatchedShowResults): MatchedShowResults = this.copy(
        burningSeries = burningSeries ?: other.burningSeries,
        serienStream = serienStream ?: other.serienStream
    )

    fun isEmpty(): Boolean {
        return burningSeries == null && serienStream == null
    }
}