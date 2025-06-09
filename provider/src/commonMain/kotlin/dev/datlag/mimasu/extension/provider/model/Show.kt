package dev.datlag.mimasu.extension.provider.model

import dev.datlag.mimasu.extension.matcher.TokenAware
import dev.datlag.mimasu.extension.matcher.TokenResult
import dev.datlag.mimasu.extension.matcher.Tokenizer
import dev.datlag.tooling.scopeCatching
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
sealed interface Show {

    @Serializable
    data class Request(
        val tmdbId: Int? = null,
        val imdbId: String? = null,
        val wikidataId: String? = null,
        val title: String? = null,
        val originalTitle: String? = null,
        val firstReleaseYear: Int? = null,
        val isAnimation: Boolean? = null,
        val numberOfNormalSeasons: Int? = null,
        val hasSpecialSeason: Boolean? = null,
        val season: Int? = null,
        val appLocale: String? = null
    ) : Show, TokenAware {

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

    @Serializable
    data class EpisodeRequest(
        val episodeNumber: Int? = null,
        val episodeTitle: String? = null,
        val numberOfNormalSeasons: Int? = null,
        val hasSpecialSeason: Boolean? = null,
        val season: Int? = null,
    ) : Show {

        companion object {
            @OptIn(ExperimentalSerializationApi::class)
            operator fun invoke(bytes: ByteArray?): EpisodeRequest? {
                if (bytes == null || bytes.isEmpty()) {
                    return null
                }

                return scopeCatching {
                    protobuf.decodeFromByteArray<EpisodeRequest>(bytes)
                }.getOrNull()
            }
        }
    }

    @Serializable
    data class Response(
        val recapRange: Skipable? = null,
        val introRange: Skipable? = null,
        val outroRange: Skipable? = null,
        val previewRange: Skipable? = null,
        val sources: Map<SourceInfo, List<String>> = emptyMap()
    ) : Show {

        @OptIn(ExperimentalSerializationApi::class)
        fun toByteArray(): ByteArray {
            return protobuf.encodeToByteArray(this)
        }

        @Serializable
        data class Skipable(
            val start: Long? = null,
            val end: Long? = null
        )

        @Serializable
        data class SourceInfo(
            val sourceTitle: String? = null,
            val sourceKey: String? = null,
            val locale: String? = null
        )
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val protobuf = ProtoBuf {
            encodeDefaults = false
        }
    }
}