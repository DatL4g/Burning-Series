package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.activate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.LocalDI
import org.kodein.di.DI

class ActivateDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val series: Series,
    override val episode: Series.Episode,
    private val onDismiss: () -> Unit,
    private val onActivate: (Series, Series.Episode) -> Unit
) : ActivateComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            ActivateDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

    override fun activate() {
        onActivate(series, episode)
        dismiss()
    }
}