package dev.datlag.burningseries.ui.dialog.example

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.ui.dialog.DialogComponent

class ExampleDialogComponent(
    componentContext: ComponentContext,
    override val message: String,
    private val onDismissed: () -> Unit
) : DialogComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }
}