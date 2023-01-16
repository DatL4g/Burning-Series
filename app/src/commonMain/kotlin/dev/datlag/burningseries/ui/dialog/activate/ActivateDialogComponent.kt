package dev.datlag.burningseries.ui.dialog.activate

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import org.kodein.di.DI

class ActivateDialogComponent(
    componentContext: ComponentContext,
    override val episode: Series.Episode,
    private val onDismissed: () -> Unit,
    private val onActivate: (Series.Episode) -> Unit,
    override val di: DI
) : ActivateComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }

    override fun onConfirmActivate() {
        onActivate(episode)
    }
}