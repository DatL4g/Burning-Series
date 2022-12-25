package dev.datlag.burningseries.ui.screen.about

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class AboutScreenComponent(
    componentContext: ComponentContext,
    override val onGoBack: () -> Unit,
    override val di: DI
) : AboutComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        AboutScreen(this)
    }
}