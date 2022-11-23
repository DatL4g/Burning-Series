package dev.datlag.burningseries.ui.navigation

import androidx.compose.runtime.Composable
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
import com.arkivanov.essenty.parcelable.Parcelable
import dev.datlag.burningseries.ui.screen.login.LoginScreenComponent

class NavHostComponent(
    componentContext: ComponentContext
) : Component, ComponentContext by componentContext {

    private val navigation = StackNavigation<ScreenConfig>()
    private val stack = childStack(
        source = navigation,
        initialConfiguration = ScreenConfig.Login,
        childFactory = ::createScreenComponent
    )

    private fun createScreenComponent(
        screenConfig: ScreenConfig,
        componentContext: ComponentContext
    ): Component {
        return when (screenConfig) {
            is ScreenConfig.Login -> LoginScreenComponent(componentContext, ::onLoginClicked)
        }
    }

    private fun onGoBackClicked() {
        navigation.pop()
    }

    private fun onLoginClicked(user: String, password: String) {
        println("Clicked Login: $user, $password")
    }

    @OptIn(ExperimentalDecomposeApi::class)
    @Composable
    override fun render() {
        Children(
            stack = stack,
            animation = stackAnimation(fade().plus(scale()))
        ) {
            it.instance.render()
        }
    }

    private sealed class ScreenConfig : Parcelable {
        object Login : ScreenConfig()
    }

}