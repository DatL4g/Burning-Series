package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
public data class Home(
    @SerialName("latestEpisodes") public val episodes: List<Episode> = emptyList(),
    @SerialName("latestSeries") public val series: List<Series> = emptyList()
) : Parcelable {

    @Parcelize
    @Serializable
    public data class Episode(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("infoText") val info: String = String(),
        @SerialName("infoFlags") val flags: List<Flag> = emptyList(),
        @SerialName("isNsfw") val isNsfw: Boolean = false,
        @SerialName("cover") val cover: Cover
    ) : Parcelable {

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

        @Parcelize
        @Serializable
        public data class Flag(
            @SerialName("class") val `class`: String,
            @SerialName("title") val title: String
        ) : Parcelable
    }

    @Parcelize
    @Serializable
    public data class Series(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("isNsfw") val isNsfw: Boolean = false,
        @SerialName("cover") val cover: Cover
    ) : Parcelable
}
