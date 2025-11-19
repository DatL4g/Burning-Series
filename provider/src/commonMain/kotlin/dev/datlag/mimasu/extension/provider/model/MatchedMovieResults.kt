package dev.datlag.mimasu.extension.provider.model

import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.streamkiste.model.Browse.Item as SKItem
import kotlinx.serialization.Serializable

@Serializable
data class MatchedMovieResults(
    val streamkiste: MatchResult<SKItem>? = null
) {
    operator fun plus(other: MatchedMovieResults): MatchedMovieResults = this.copy(
        streamkiste = streamkiste ?: other.streamkiste
    )

    fun isEmpty(): Boolean {
        return streamkiste == null
    }
}
