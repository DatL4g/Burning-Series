package dev.datlag.burningseries.model

import dev.datlag.burningseries.model.serializer.SerializableImmutableList
import dev.datlag.burningseries.model.serializer.SerializableImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Home(
    @SerialName("latestEpisodes") val episodes: SerializableImmutableSet<Episode> = persistentSetOf(),
    @SerialName("latestSeries") val series: SerializableImmutableSet<Series> = persistentSetOf()
) {

    @Serializable
    data class Episode(
        @SerialName("title") val fullTitle: String,
        @SerialName("href") override val href: String,
        @SerialName("infoText") val info: String? = null,
        @SerialName("infoFlags") val flags: SerializableImmutableSet<Flag> = persistentSetOf(),
        @SerialName("cover") val coverHref: String? = null
    ) : SeriesData() {

        @Transient
        private val seriesAndEpisodeMatch = Regex(
            "^(.+(:|\\|))(.+)\$",
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        ).find(fullTitle)

        @Transient
        val series: String? = seriesAndEpisodeMatch?.groupValues?.get(1)?.trim()?.dropLast(1)?.ifBlank { null }

        @Transient
        val episode: String? = seriesAndEpisodeMatch?.groupValues?.get(3)?.trim()?.ifBlank { null }

        override val title: String
            get() = series ?: fullTitle

        override val mainTitle: String
            get() = series ?: super.mainTitle

        override val subTitle: String?
            get() = episode ?: super.subTitle

        @Serializable
        data class Flag(
            @SerialName("class") val clazz: String,
            @SerialName("title") val title: String?
        ) {
            @Transient
            val bestCountryCode: String? = clazz.split(" ").firstOrNull {
                !it.equals("flag", true) && it.contains("flag", true)
            }?.replace("flag", "", true)?.replace("-", "", true)
        }
    }

    @Serializable
    data class Series(
        @SerialName("title") override val title: String,
        @SerialName("href") override val href: String,
        @SerialName("coverHref") val coverHref: String? = null
    ) : SeriesData()
}
