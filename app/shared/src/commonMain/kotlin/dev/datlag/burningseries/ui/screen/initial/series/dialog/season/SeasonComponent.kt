package dev.datlag.burningseries.ui.screen.initial.series.dialog.season

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.DialogComponent

interface SeasonComponent : DialogComponent {

    val defaultSeason: Series.Season
    val seasons: List<Series.Season>

    fun onConfirm(season: Series.Season)
}