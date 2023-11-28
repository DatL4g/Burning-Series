package dev.datlag.burningseries.shared.ui.screen.initial.series

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series

@Parcelize
sealed class DialogConfig : Parcelable {

    @Parcelize
    data class Season(
        val selected: Series.Season,
        val seasons: List<Series.Season>
    ) : DialogConfig()

    @Parcelize
    data class Language(
        val selected: Series.Language,
        val languages: List<Series.Language>
    ) : DialogConfig()

    @Parcelize
    data class StreamUnavailable(
        val series: Series,
        val episode: Series.Episode
    ) : DialogConfig()
}