package dev.datlag.burningseries.shared.ui.screen.video.dialog.cast

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.kast.Device
import org.kodein.di.DI

class CastDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit,
) : CastComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CastDialog(this)
    }

    override fun dismiss() {
        onDismiss()
    }
}