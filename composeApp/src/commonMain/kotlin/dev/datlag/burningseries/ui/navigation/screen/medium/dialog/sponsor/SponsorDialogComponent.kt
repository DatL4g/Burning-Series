package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.sponsor

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.other.UserHelper
import org.kodein.di.DI
import org.kodein.di.instance
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow

class SponsorDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit
) : SponsorComponent, ComponentContext by componentContext {

    private val authFlow by instance<CodeAuthFlow>()
    private val userHelper by instance<UserHelper>()
    override val isLoggedIn: Boolean
        get() = userHelper.isLoggedIn

    @Composable
    override fun render() {
        onRender {
            SponsorDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

    override fun login() {
        launchIO {
            userHelper.login(authFlow)
        }
    }
}