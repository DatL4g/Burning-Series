package dev.datlag.burningseries.model

import dev.datlag.burningseries.model.common.getDigitsOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Series(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
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

    @Serializable
    data class Season(
        @SerialName("value") val value: Int,
        @SerialName("title") val title: String
    )

    @Serializable
    data class Language(
        @SerialName("value") val value: String,
        @SerialName("title") val title: String
    )

    @Serializable
    data class Episode(
        @SerialName("number") val number: String,
        @SerialName("title") val title: String,
        @SerialName("href") val href: String
    )
}