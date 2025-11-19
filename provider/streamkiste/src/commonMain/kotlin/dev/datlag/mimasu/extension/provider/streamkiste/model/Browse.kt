package dev.datlag.mimasu.extension.provider.streamkiste.model

import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import dev.datlag.tooling.setFrom
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Browse(
    @SerialName("movies") val movies: Set<Item> = emptySet(),
    @SerialName("tvseries") private val tvSeries: Set<Item> = emptySet(),
    @SerialName("series") private val _series: Set<Item> = emptySet()
) {

    @Transient
    val series = setFrom(tvSeries, _series)

    @Transient
    val all = setFrom(movies, series)

    @Serializable
    data class Item(
        @SerialName("_id") val id: String,
        @SerialName("title") val title: String? = null,
        @SerialName("year") private val _year: String? = null,
        @SerialName("genres") private val _genres: String? = null
    ) : TokenAware {

        @Transient
        val year = _year?.toIntOrNull()?.takeIf { it > 0 }

        @Transient
        val genres = _genres?.trim()?.split(',').orEmpty().mapNotNull {
            it.trim().ifBlank { null }
        }

        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(title)

        @Transient
        private val numbers = tokenResult.tokens.mapNotNull { token ->
            token.value.toIntOrNull()
        }.toSet()

        @Transient
        val releaseYear: Int? = year ?: numbers.firstNotNullOfOrNull { token ->
            token.takeIf { it in 1000..currentYear }
        }

        @Transient
        val alternativeTokenResult: TokenResult? = releaseYear?.let { year ->
            tokenResult.copy(
                tokens = tokenResult.tokens.filterNot { it.value == year.toString() }
            )
        }

        @Transient
        val isAnimation = genres.any {
            when {
                it.equals("anime", ignoreCase = true) -> true
                it.equals("animation", ignoreCase = true) -> true
                it.startsWith("anime", ignoreCase = true) -> true
                it.startsWith("animation", ignoreCase = true) -> true
                it.equals("cartoon", ignoreCase = true) -> true
                else -> false
            }
        }
    }

    companion object {
        @OptIn(ExperimentalTime::class)
        private val currentYear by lazy {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        }
    }
}
