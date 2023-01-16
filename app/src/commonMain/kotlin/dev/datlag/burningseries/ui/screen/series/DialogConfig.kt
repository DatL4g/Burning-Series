package dev.datlag.burningseries.ui.screen.series

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series

@Parcelize
sealed class DialogConfig : Parcelable {
    data class Language(
        val languages: List<Series.Language>,
        val selectedLanguage: String
    ) : DialogConfig()

    data class Season(
        val seasons: List<Series.Season>,
        val selectedSeason: Series.Season?
    ) : DialogConfig()

    data class NoStream(val episode: Series.Episode) : DialogConfig()

    data class Activate(val episode: Series.Episode) : DialogConfig()
}