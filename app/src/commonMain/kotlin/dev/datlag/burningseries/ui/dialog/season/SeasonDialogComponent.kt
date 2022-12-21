package dev.datlag.burningseries.ui.dialog.season

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.repository.SeriesRepository
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.instance

class SeasonDialogComponent(
    componentContext: ComponentContext,
    override val seasons: List<Series.Season>,
    override val selectedSeason: Series.Season?,
    private val onDismissed: () -> Unit,
    private val onSelected: (Series.Season) -> Unit,
    override val di: DI
) : SeasonComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }

    override fun onConfirmNewSeason(season: Series.Season) {
        onSelected(season)
    }
}