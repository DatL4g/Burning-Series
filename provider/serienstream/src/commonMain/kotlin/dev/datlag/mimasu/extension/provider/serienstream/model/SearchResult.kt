package dev.datlag.mimasu.extension.provider.serienstream.model

import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SearchResult(
    @SerialName("name") val name: String,
    @SerialName("link") val link: String,
    @SerialName("description") val description: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("productionYear") val productionYear: String? = null
) : TokenAware {

    @Transient
    val releaseYear = productionYear?.split("-")?.firstOrNull()?.replace(productionSanitizeRegex, "")?.trim()?.toIntOrNull()

    @Transient
    override val tokenResult: TokenResult = Tokenizer.tokenize(name)

    companion object {
        private val productionSanitizeRegex = "\\D".toRegex()
    }
}
