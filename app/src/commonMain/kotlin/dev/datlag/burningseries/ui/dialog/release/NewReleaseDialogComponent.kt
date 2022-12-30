package dev.datlag.burningseries.ui.dialog.release

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Release
import org.kodein.di.DI

class NewReleaseDialogComponent(
    componentContext: ComponentContext,
    override val newRelease: Release,
    private val onDismissed: () -> Unit,
    override val di: DI
) : NewReleaseComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }
}