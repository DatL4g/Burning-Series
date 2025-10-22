package dev.datlag.mimasu.extension.provider.burningseries.model

import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SearchItem(
    val title: String,
    val alternativeTitles: Set<String>,
    val href: String,
    val genre: String?,
    val isAnimation: Boolean? = genre?.let {
        it.equals("animation", ignoreCase = true)
                || it.contains("anime", ignoreCase = true)
                || it.equals("zeichentrick", ignoreCase = true)
    },
): TokenAware, SeriesData {

    constructor(title: String, href: String, genre: String?) : this(
        title = title.split('|').filterNot { it.isBlank() }.firstOrNull()?.trim() ?: title,
        alternativeTitles = title.split('|').filterNot { it.isBlank() }.drop(1).map { it.trim() }.toSet(),
        href = href,
        genre = genre
    )

    @Transient
    override val info: SeriesData.Info = SeriesData.fromHref(href)

    @Transient
    override val tokenResult: TokenResult = Tokenizer.tokenize(title)

    @Transient
    val alternativeTokenResults: Set<TokenResult> = alternativeTitles.mapNotNull {
        if (it.isBlank()) {
            return@mapNotNull null
        }

        Tokenizer.tokenize(it)
    }.toSet()

    @Transient
    private val numbers = (tokenResult.tokens.mapNotNull { token ->
        token.value.toIntOrNull()
    } + alternativeTokenResults.flatMap { alternative ->
        alternative.tokens.mapNotNull { token -> token.value.toIntOrNull() }
    }).toSet()

    @Transient
    val releaseYear = numbers.firstNotNullOfOrNull { token ->
        token.takeIf { it in 1000..BurningSeries.currentYear }
    }

}
