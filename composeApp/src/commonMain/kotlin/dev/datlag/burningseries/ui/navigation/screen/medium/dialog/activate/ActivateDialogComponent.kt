package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class ActivateDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit,
    private val onActivate: () -> Unit
) : ActivateComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        onRender {
            ActivateDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

    override fun activate() {
        onActivate()
    }
}