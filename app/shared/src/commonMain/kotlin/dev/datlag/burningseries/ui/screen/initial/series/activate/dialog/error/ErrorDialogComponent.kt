package dev.datlag.burningseries.ui.screen.initial.series.activate.dialog.error

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class ErrorDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismissed: () -> Unit
) : ErrorComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        ErrorDialog(this)
    }

    override fun dismiss() {
        onDismissed()
    }
}