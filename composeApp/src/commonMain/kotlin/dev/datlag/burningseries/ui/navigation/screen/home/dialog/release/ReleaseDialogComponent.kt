package dev.datlag.burningseries.ui.navigation.screen.home.dialog.release

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.github.model.UserAndRelease
import org.kodein.di.DI

class ReleaseDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val release: UserAndRelease.Release,
    private val onDismiss: () -> Unit
) : ReleaseComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        onRender {
            ReleaseDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }
}