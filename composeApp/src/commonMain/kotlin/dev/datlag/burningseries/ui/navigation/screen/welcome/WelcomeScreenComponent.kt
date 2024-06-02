package dev.datlag.burningseries.ui.navigation.screen.welcome

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.compose.withMainContext
import org.kodein.di.DI
import org.kodein.di.instance

class WelcomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onHome: () -> Unit
) : WelcomeComponent, ComponentContext by componentContext {

    private val settings by instance<Settings.PlatformAppSettings>()

    @Composable
    override fun render() {
        onRender {
            WelcomeScreen(this)
        }
    }

    override fun start(language: Language) {
        launchIO {
            settings.setLanguage(language)
            withMainContext {
                onHome()
            }
        }
    }
}