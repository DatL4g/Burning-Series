package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.common.getDigitsOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Series(
    @SerialName("title") val title: String,
    @SerialName("season") val season: String,
    @SerialName("description") val description: String,
    @SerialName("selectedLanguage") val selectedLanguage: String,
    @SerialName("cover") val cover: Cover,
    @SerialName("isNsfw") val isNsfw: Boolean,
    @SerialName("infos") val infos: List<Info>,
    @SerialName("languages") val languages: List<Language>,
    @SerialName("seasons") val seasons: List<Season>,
    @SerialName("episodes") val episodes: List<Episode>,
    @SerialName("linkedSeries") val linkedSeries: List<Linked>,
    @SerialName("href") val href: String
) : Parcelable {

    fun currentSeason(seasons: List<Season> = this.seasons): Season? {
        return seasons.find {
            it.title.equals(season, true)
                    || it.title.trim().equals(season.trim(), true)
                    || it.title.equals(season.toIntOrNull()?.toString(), true)
                    || it.title.toIntOrNull()?.toString()?.equals(season, true) == true
                    || it.title.toIntOrNull()?.toString()?.equals(season.toIntOrNull()?.toString(), true) == true
                    || it.title.equals(season.getDigitsOrNull(), true)
                    || it.title.getDigitsOrNull()?.equals(season, true) == true
                    || it.title.getDigitsOrNull()?.equals(season.getDigitsOrNull(), true) == true
        }
    }

    fun hrefBuilder(season: Int?, language: String = selectedLanguage): String {
        var verifiedHref = href
        if (verifiedHref.startsWith('/')) {
            verifiedHref = verifiedHref.substring(1)
        }
        if (verifiedHref.endsWith('/')) {
            verifiedHref = verifiedHref.substring(0..(verifiedHref.length - 2))
        }
        val splitHref = verifiedHref.split('/')
        var joinedHref = splitHref.joinToString(separator = "/", limit = 2, truncated = String())
        if (!joinedHref.endsWith('/')) {
            joinedHref += '/'
        }
        joinedHref += season?.toString() ?: String()
        if (!joinedHref.endsWith('/')) {
            joinedHref += '/'
        }
        joinedHref += language
        return joinedHref
    }

    @Parcelize
    @Serializable
    data class Info(
        @SerialName("header") val header: String,
        @SerialName("data") val data: String
    ): Parcelable {
        fun isGenre(): Boolean {
            return this.header.trim().equals("Genre", true) || this.header.trim().equals("Genres", true)
        }

        fun trimmedData(): String {
            var newData = data.trim()
            if (newData.contains(',')) {
                newData = newData.split(',').joinToString { it.trim() }
            }
            return newData
        }
    }

    @Parcelize
    @Serializable
    data class Language(
        @SerialName("value") val value: String,
        @SerialName("text") val text: String
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Season(
        @SerialName("title") val title: String,
        @SerialName("value") val value: Int
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Episode(
        @SerialName("number") val number: String,
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("hoster") val hoster: List<Hoster>
    ) : Parcelable {

        @Parcelize
        @Serializable
        data class Hoster(
            @SerialName("title") val title: String,
            @SerialName("href") val href: String
        ) : Parcelable
    }

    @Parcelize
    @Serializable
    data class Linked(
        @SerialName("isSpinOff") val isSpinOff: Boolean,
        @SerialName("isMainStory") val isMainStory: Boolean,
        @SerialName("title") val title: String,
        @SerialName("href") val href: String
    ) : Parcelable
}
