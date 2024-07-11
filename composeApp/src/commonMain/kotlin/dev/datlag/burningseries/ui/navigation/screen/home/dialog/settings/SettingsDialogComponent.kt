package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.other.UserHelper
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.compose.withMainContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.kodein.di.DI
import org.kodein.di.instance
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow

class SettingsDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit,
    private val onAbout: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    private val settings by instance<Settings.PlatformAppSettings>()
    override val language: Flow<Language?> = settings.language.flowOn(ioDispatcher())

    private val userHelper by instance<UserHelper>()
    private val authFlow by instance<CodeAuthFlow>()
    override val user: Flow<UserAndRelease.User?> = userHelper.user

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

    @OptIn(ExperimentalOpenIdConnect::class)
    override fun login() {
        launchIO {
            userHelper.login(authFlow)
        }
    }

    override fun logout() {
        launchIO {
            userHelper.logout()
            withMainContext {
                onDismiss()
            }
        }
    }

    override fun about() {
        onAbout()
    }
}