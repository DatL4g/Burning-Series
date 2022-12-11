package dev.datlag.burningseries.ui.dialog.nostream

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import org.kodein.di.DI

class NoStreamDialogComponent(
    componentContext: ComponentContext,
    private val episode: Series.Episode,
    private val onDismissed: () -> Unit,
    private val onActivate: (Series.Episode) -> Unit,
    override val di: DI
) : NoStreamComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }

    override fun onConfirmActivate() {
        onActivate(episode)
    }
}