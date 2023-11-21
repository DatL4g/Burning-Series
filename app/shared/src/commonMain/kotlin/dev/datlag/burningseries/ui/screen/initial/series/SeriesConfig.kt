package dev.datlag.burningseries.ui.screen.initial.series

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series

@Parcelize
sealed class SeriesConfig : Parcelable {

    @Parcelize
    data class Activate(
        val series: Series,
        val episode: Series.Episode
    ) : SeriesConfig(), Parcelable
}