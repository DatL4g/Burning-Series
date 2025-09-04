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

/**
 * Handles show and episode related request/response in Mimasu.
 *
 * Data is exchanged between Mimasu and extensions using AIDL.
 * Take a look at the AIDL interfaces!
 */
@Serializable
sealed interface Show {

    /**
     * Used to search for shows.
     * This data comes from Mimasu (the base app) and can be used in extensions to search / filter according shows.
     *
     * !!! [TokenAware] is exclusive to this extension app and can't be used anywhere else !!!
     *
     * @param tmdbId ID of the show on TMDB
     * @param imdbId ID of the show on IMDB
     * @param wikidataId ID of the show on Wikidata
     * @param title localized show title (depending on the user language)
     * @param originalTitle show title in it's original language (independent from user language)
     * @param firstReleaseYear year the show was first released / aired in
     * @param isAnimation whether the show is an animation / anime or not
     * @param numberOfNormalSeasons !!! currently not provided !!!
     * @param hasSpecialSeason !!! currently not provided !!!
     * @param season !!! currently not provided !!!
     * @param appLocale user language used in the app
     */
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

        /**
         * Splits the title into searchable tokens.
         *
         * !!! Exclusive to this extension app !!!
         */
        @Transient
        override val tokenResult: TokenResult = Tokenizer.tokenize(title)

        /**
         * Splits the original into searchable tokens.
         *
         * !!! Exclusive to this extension app !!!
         */
        @Transient
        val originalTokenResult: TokenResult = Tokenizer.tokenize(originalTitle)

        companion object {

            /**
             * Constructor parsing the data coming from Mimasu to [Show.Request]
             */
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

    /**
     * Used to search for episodes in shows to check their availability and request streams.
     * This data comes from Mimasu (the base app) and can be used in extensions to search / filter according episodes.
     *
     * @param episodeNumber number of the episode in it's specific season
     * @param episodeTitle title of the episode (depending on the user language)
     * @param numberOfNormalSeasons !!! currently not provided !!!
     * @param hasSpecialSeason !!! currently not provided !!!
     * @param season currently selected season
     * @param appLocale user language used in the app
     */
    @Serializable
    data class EpisodeRequest(
        val episodeNumber: Int? = null,
        val episodeTitle: String? = null,
        val numberOfNormalSeasons: Int? = null,
        val hasSpecialSeason: Boolean? = null,
        val season: Int? = null,
        val appLocale: String? = null
    ) : Show {

        @Transient
        val appLanguage = appLocale
            ?.substringBefore('-')
            ?.substringBefore('_')
            ?.ifBlank { null }
            ?.trim()

        companion object {

            /**
             * Constructor parsing the data coming from Mimasu to [Show.EpisodeRequest]
             */
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

    /**
     * Response for streams.
     *
     * @param recapRange !!! currently not used !!!
     * @param introRange !!! currently not used !!!
     * @param outroRange !!! currently not used !!!
     * @param previewRange !!! currently not used !!!
     * @param sources streaming sources and links. Links have to be full URLs.
     * @see SourceInfo
     */
    @Serializable
    data class Response(
        val recapRange: Skipable? = null,
        val introRange: Skipable? = null,
        val outroRange: Skipable? = null,
        val previewRange: Skipable? = null,
        val sources: Map<SourceInfo, List<String>> = emptyMap()
    ) : Show {

        /**
         * Converts the response to data readable in Mimasu.
         */
        @OptIn(ExperimentalSerializationApi::class)
        fun toByteArray(): ByteArray {
            return protobuf.encodeToByteArray(this)
        }

        /**
         * Declares a skip-able time segment in a stream.
         * Example for intros or outros.
         *
         * !!! Currently unused, no need to provide !!!
         */
        @Serializable
        data class Skipable(
            val start: Long? = null,
            val end: Long? = null
        )

        /**
         * Declares where the stream is coming from.
         *
         * @param sourceTitle something like the original website name
         * @param sourceLocale title of the locale e.g. "English" or "German"
         * @param locale locale of the stream e.g. "en" or "de"
         */
        @Serializable
        data class SourceInfo(
            val sourceTitle: String? = null,
            val sourceLocale: String? = null,
            val locale: String? = null
        )
    }

    companion object {

        /**
         * Used to convert the data between Mimasu and extensions.
         */
        @OptIn(ExperimentalSerializationApi::class)
        private val protobuf = ProtoBuf {
            encodeDefaults = false
        }
    }
}