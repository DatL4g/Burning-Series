package dev.datlag.burningseries.shared.ui.screen.initial.home.dialog.sekret

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.shared.LocalDI
import org.kodein.di.DI

class SekretDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismissed: () -> Unit,
) : SekretComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            SekretDialog(this)
        }
    }

    override fun dismiss() {
        onDismissed()
    }
}