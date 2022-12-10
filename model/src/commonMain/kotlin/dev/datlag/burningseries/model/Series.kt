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
    @SerialName("linkedSeries") val linkedSeries: List<Linked>
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

    @Parcelize
    @Serializable
    data class Info(
        @SerialName("header") val header: String,
        @SerialName("data") val data: String
    ): Parcelable

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
