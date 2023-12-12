package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.unavailable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.LocalDI
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
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            UnavailableDialog(this)
        }
    }

    override fun dismiss() {
        onDismissed()
    }

    override fun activate() {
        onActivate(series, episode)
        dismiss()
    }
}