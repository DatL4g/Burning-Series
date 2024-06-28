package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.sponsor

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class SponsorDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit
) : SponsorComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        onRender {
            SponsorDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }
}