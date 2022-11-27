package dev.datlag.burningseries.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
        @SerialName("cover") val cover: Cover
    ) {

        fun getSeriesAndEpisode(): Pair<String, String> {
            val match = Regex(
                "^(.+(:|\\|))(.+)\$",
                setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
            ).find(title)

            return Pair(
                match?.groupValues?.get(1)?.trim()?.dropLast(1) ?: String(),
                match?.groupValues?.get(3)?.trim() ?: String()
            )
        }

        @Serializable
        data class Flag(
            @SerialName("class") val `class`: String,
            @SerialName("title") val title: String
        )
    }

    @Serializable
    data class Series(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("isNsfw") val isNsfw: Boolean = false,
        @SerialName("cover") val cover: Cover
    )
}
