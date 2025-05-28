package dev.datlag.mimasu.extension.provider.burningseries.model

import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.Tokenizer
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SearchItem(
    val title: String,
    val href: String,
    val genre: String?
): TokenAware {

    @Transient
    override val tokens: List<String> = Tokenizer.tokenize(title)

    @Transient
    private val numbers = tokens.mapNotNull { token -> token.toIntOrNull() }

    @Transient
    val releaseYear = numbers.firstNotNullOfOrNull { token ->
        token.takeIf { it in 1000..BurningSeries.currentYear }
    }

}
