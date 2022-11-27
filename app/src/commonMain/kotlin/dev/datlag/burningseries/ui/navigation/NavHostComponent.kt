package dev.datlag.burningseries.ui.navigation

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import dev.datlag.burningseries.common.expiration
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.showedLogin
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.module.DataStoreModule
import dev.datlag.burningseries.module.PlatformModule
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.ui.screen.home.HomeScreenComponent
import dev.datlag.burningseries.ui.screen.login.LoginScreenComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.kodein.di.*

class NavHostComponent private constructor(
    componentContext: ComponentContext,
    override val di: DI
) : Component, ComponentContext by componentContext {

    private val navigation = StackNavigation<ScreenConfig>()
    private val stack = childStack(
        source = navigation,
        initialStack = {
            val userDataStore: DataStore<UserSettings> by di.instance()
            val showedLogin = userDataStore.showedLogin.getValueBlocking(false)
            val defaultScreen = if (showedLogin) {
                ScreenConfig.Home
            } else {
                ScreenConfig.Login
            }
            listOf(defaultScreen)
        },
        childFactory = ::createScreenComponent
    )

    private fun createScreenComponent(
        screenConfig: ScreenConfig,
        componentContext: ComponentContext
    ): Component {
        return when (screenConfig) {
            is ScreenConfig.Login -> LoginScreenComponent(componentContext, ::onLoginClicked, di)
            is ScreenConfig.Home -> HomeScreenComponent(componentContext, di)
            else -> HomeScreenComponent(componentContext, di)
        }
    }

    private fun onGoBackClicked() {
        navigation.pop()
    }

    private fun onLoginClicked() {
        navigation.push(ScreenConfig.Home)
    }

    @OptIn(ExperimentalDecomposeApi::class)
    @Composable
    override fun render() {
        Children(
            stack = stack,
            animation = stackAnimation(fade() + scale())
        ) {
            it.instance.render()
        }
    }

    companion object {
        fun create(componentContext: ComponentContext, di: DI): NavHostComponent {
            return NavHostComponent(componentContext, di)
        }
    }

}