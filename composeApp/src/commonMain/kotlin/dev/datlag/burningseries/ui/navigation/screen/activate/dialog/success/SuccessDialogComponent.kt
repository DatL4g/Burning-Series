package dev.datlag.burningseries.ui.navigation.screen.activate.dialog.success

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection
import org.kodein.di.DI

class SuccessDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val stream: ImmutableCollection<DirectLink>,
    private val onDismiss: () -> Unit,
    private val onBack: () -> Unit,
    private val onWatch: (ImmutableCollection<DirectLink>) -> Unit
) : SuccessComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        onRender {
            SuccessDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

    override fun back() {
        onBack()
    }

    override fun watch(stream: ImmutableCollection<DirectLink>) {
        onWatch(stream)
    }
}