package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.unavailable

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import org.kodein.di.DI

class UnavailableDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val series: Series,
    override val episode: Series.Episode,
    private val onDismissed: () -> Unit,
    private val onActivate: (Series, Series.Episode) -> Unit
) : UnavailableComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        UnavailableDialog(this)
    }

    override fun dismiss() {
        onDismissed()
    }

    override fun activate() {
        onActivate(series, episode)
        dismiss()
    }
}