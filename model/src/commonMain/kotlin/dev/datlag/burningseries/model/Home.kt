package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Home(
    @SerialName("latestEpisodes") val episodes: List<Episode> = emptyList(),
    @SerialName("latestSeries") val series: List<Series> = emptyList()
) {

    @Serializable
    data class Episode(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("infoText") val info: String = String(),
        @SerialName("infoFlags") val flags: List<Flag> = emptyList(),
        @SerialName("isNsfw") val isNsfw: Boolean = false,
        @SerialName("cover") val coverHref: String? = null
    ) {

        @Transient
        private val seriesAndEpisodeMatch = Regex(
            "^(.+(:|\\|))(.+)\$",
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        ).find(title)

        @Transient
        val series: String? = seriesAndEpisodeMatch?.groupValues?.get(1)?.trim()?.dropLast(1)?.ifBlank { null }

        @Transient
        val episode: String? = seriesAndEpisodeMatch?.groupValues?.get(3)?.trim()?.ifBlank { null }

        @Serializable
        data class Flag(
            @SerialName("class") val clazz: String,
            @SerialName("title") val title: String
        ) {
            @Transient
            val bestCountryCode: String? = clazz.split(" ").firstOrNull {
                !it.equals("flag", true) && it.contains("flag", true)
            }?.replace("flag", String(), true)?.replace("-", String(), true)
        }
    }

    @Serializable
    data class Series(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("isNsfw") val isNsfw: Boolean = false,
        @SerialName("coverHref") val coverHref: String? = null
    )
}