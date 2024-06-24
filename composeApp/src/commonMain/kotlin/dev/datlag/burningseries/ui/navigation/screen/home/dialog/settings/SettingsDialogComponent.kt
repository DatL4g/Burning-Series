package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.compose.ioDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    private val settings by instance<Settings.PlatformAppSettings>()
    override val language: Flow<Language?> = settings.language.flowOn(ioDispatcher())

    @Composable
    override fun render() {
        onRender {
            SettingsDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

    override fun setLanguage(language: Language) {
        launchIO {
            settings.setLanguage(language)
        }
    }
}