package dev.datlag.mimasu.extension.provider.model

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem as BSItem
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem as SAWItem
import dev.datlag.mimasu.extension.provider.streamkiste.model.Browse.Item as SKItem
import kotlinx.serialization.Serializable

@Serializable
data class MatchedShowResults(
    val burningSeries: MatchResult<BSItem>? = null,
    val serienStream: MatchResult<SAWItem>? = null,
    val streamkiste: MatchResult<SKItem>? = null
) {
    operator fun plus(other: MatchedShowResults): MatchedShowResults = this.copy(
        burningSeries = burningSeries ?: other.burningSeries,
        serienStream = serienStream ?: other.serienStream,
        streamkiste = streamkiste ?: other.streamkiste
    )

    fun isEmpty(): Boolean {
        return burningSeries == null && serienStream == null && streamkiste == null
    }
}