package dev.datlag.mimasu.extension.provider.serienstream.model

import com.fleeksoft.ksoup.Ksoup
import dev.datlag.mimasu.extension.ksoup.parseGet
import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import io.ktor.client.HttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed interface SearchItem : TokenAware {

    val name: String
    val slug: String
    val description: String?
    val cover: String?
    val productionYear: String?

    val releaseYear: Int?
    val alternativeTokenResult: TokenResult?

    @Serializable
    data class AniWorld(
        @SerialName("name") override val name: String,
        @SerialName("link") override val slug: String,
        @SerialName("description") override val description: String? = null,
        @SerialName("cover") override val cover: String? = null,
        @SerialName("productionYear") override val productionYear: String? = null
    ) : SearchItem {

        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(name)

        @Transient
        override val releaseYear: Int? = productionYear?.split("-")?.firstOrNull()?.replace(productionSanitizeRegex, "")?.trim()?.toIntOrNull()

        @Transient
        override val alternativeTokenResult: TokenResult? = releaseYear?.let { year ->
            tokenResult.copy(
                tokens = tokenResult.tokens.filterNot { it.value == year.toString() }
            )
        }
    }

    @Serializable
    data class SerienStream(
        @SerialName("name") override val name: String,
        @SerialName("link") override val slug: String,
        @SerialName("description") override val description: String? = null,
        @SerialName("cover") override val cover: String? = null,
        @SerialName("productionYear") override val productionYear: String? = null
    ) : SearchItem {

        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(name)

        @Transient
        override val releaseYear: Int? = productionYear?.split("-")?.firstOrNull()?.replace(productionSanitizeRegex, "")?.trim()?.toIntOrNull()

        @Transient
        override val alternativeTokenResult: TokenResult? = releaseYear?.let { year ->
            tokenResult.copy(
                tokens = tokenResult.tokens.filterNot { it.value == year.toString() }
            )
        }
    }

    companion object {
        private val productionSanitizeRegex = "\\D".toRegex()
    }
}