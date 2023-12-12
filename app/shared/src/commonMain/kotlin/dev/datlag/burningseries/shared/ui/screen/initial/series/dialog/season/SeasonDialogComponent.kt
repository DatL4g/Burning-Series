package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.season

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.LocalDI
import org.kodein.di.DI

class SeasonDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val defaultSeason: Series.Season,
    override val seasons: List<Series.Season>,
    private val onDismissed: () -> Unit,
    private val onSelected: (Series.Season) -> Unit
) : SeasonComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            SeasonDialog(this)
        }
    }

    override fun dismiss() {
        onDismissed()
    }

    override fun onConfirm(season: Series.Season) {
        onSelected(season)
        dismiss()
    }
}