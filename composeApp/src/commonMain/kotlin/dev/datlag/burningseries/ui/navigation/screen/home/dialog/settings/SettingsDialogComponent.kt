package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.github.UserAndReleaseRepository
import dev.datlag.burningseries.github.UserAndReleaseState
import dev.datlag.burningseries.other.UserHelper
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.compose.ioDispatcher
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.kodein.di.DI
import org.kodein.di.instance
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler

class SettingsDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    private val settings by instance<Settings.PlatformAppSettings>()
    override val language: Flow<Language?> = settings.language.flowOn(ioDispatcher())

    private val userHelper by instance<UserHelper>()
    private val authFlow by instance<CodeAuthFlow>()
    private val userAndReleaseRepository by instance<UserAndReleaseRepository>()
    override val userAndRelease: Flow<UserAndReleaseState> = userAndReleaseRepository.userAndRelease.flowOn(
        context = ioDispatcher()
    )

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
    override fun auth() {
        launchIO {
            userHelper.login(authFlow)
        }
    }
}