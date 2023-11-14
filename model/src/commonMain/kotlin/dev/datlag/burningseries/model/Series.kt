package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.common.getDigitsOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Series(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("cover") val coverHref: String? = null,
    @SerialName("href") val href: String,
    @SerialName("seasonTitle") val seasonTitle: String,
    @SerialName("selectedLanguage") val selectedLanguage: String,
    @SerialName("seasons") val seasons: List<Season>,
    @SerialName("languages") val languages: List<Language>,
    @SerialName("episodes") val episodes: List<Episode>
) {

    val currentSeason: Season? by lazy(LazyThreadSafetyMode.NONE) {
        seasons.find {
            it.title.equals(seasonTitle, true)
                    || it.title.trim().equals(seasonTitle.trim(), true)
                    || it.title.equals(seasonTitle.toIntOrNull()?.toString(), true)
                    || it.title.toIntOrNull()?.toString().equals(seasonTitle.toIntOrNull()?.toString(), true)
                    || it.title.equals(seasonTitle.getDigitsOrNull(), true)
                    || it.title.getDigitsOrNull().equals(seasonTitle, true)
                    || it.title.getDigitsOrNull().equals(seasonTitle.getDigitsOrNull(), true)
        }
    }

    val currentLanguage: Language? by lazy(LazyThreadSafetyMode.NONE) {
        languages.find {
            it.value.equals(selectedLanguage, true)
                    || it.value.trim().equals(selectedLanguage.trim(), true)
        }
    }

    fun hrefBuilder(season: Int? = currentSeason?.value, language: String = currentLanguage?.value ?: selectedLanguage): String {
        val hrefData = BSUtil.hrefDataFromHref(
            BSUtil.normalizeHref(href)
        )

        return BSUtil.fixSeriesHref(
            BSUtil.rebuildHrefFromData(
                Triple(
                    first = hrefData.first,
                    second = season?.toString() ?: hrefData.second,
                    third = language
                )
            )
        )
    }

    @Parcelize
    @Serializable
    data class Season(
        @SerialName("value") val value: Int,
        @SerialName("title") val title: String
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Language(
        @SerialName("value") val value: String,
        @SerialName("title") val title: String
    ) : Parcelable

    @Serializable
    data class Episode(
        @SerialName("number") val number: String,
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("hosters") val hosters: List<Hoster>
    ) {

        @Serializable
        data class Hoster(
            @SerialName("title") val title: String,
            @SerialName("href") val href: String
        )
    }
}