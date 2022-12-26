package dev.datlag.burningseries.ui.dialog.season

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.dialog.DialogComponent
import kotlinx.coroutines.flow.Flow

interface SeasonComponent : DialogComponent {

    val seasons: List<Series.Season>
    val selectedSeason: Series.Season?

    fun onConfirmNewSeason(season: Series.Season)
}