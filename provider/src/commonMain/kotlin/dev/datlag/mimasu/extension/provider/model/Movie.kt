package dev.datlag.mimasu.extension.provider.model

import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import dev.datlag.tooling.scopeCatching
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
sealed interface Movie {

    @Serializable
    data class Request(
        val tmdbId: Int? = null,
        val imdbId: String? = null,
        val wikidataId: String? = null,
        val title: String? = null,
        val originalTitle: String? = null,
        val firstReleaseYear: Int? = null,
        val isAnimation: Boolean? = null,
        val appLocale: String? = null
    ) : Movie, TokenAware {

        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(title)

        @Transient
        val originalTokenResult: TokenResult = Tokenizer.tokenize(originalTitle)

        companion object {

            @OptIn(ExperimentalSerializationApi::class)
            operator fun invoke(bytes: ByteArray?): Request? {
                if (bytes == null || bytes.isEmpty()) {
                    return null
                }

                return scopeCatching {
                    protobuf.decodeFromByteArray<Request>(bytes)
                }.getOrNull()
            }
        }
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val protobuf = ProtoBuf {
            encodeDefaults = false
        }
    }
}