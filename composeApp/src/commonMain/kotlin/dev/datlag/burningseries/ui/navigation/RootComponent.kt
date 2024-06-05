package dev.datlag.burningseries.ui.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.ui.navigation.screen.home.HomeScreenComponent
import dev.datlag.burningseries.ui.navigation.screen.medium.MediumScreenComponent
import dev.datlag.burningseries.ui.navigation.screen.welcome.WelcomeScreenComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance

class RootComponent(
    componentContext: ComponentContext,
    override val di: DI
) : Component, ComponentContext by componentContext {

    private val settings by instance<Settings.PlatformAppSettings>()

    private val navigation = StackNavigation<RootConfig>()
    private val stack = childStack(
        source = navigation,
        serializer = RootConfig.serializer(),
        initialConfiguration = runBlocking {
            settings.language.firstOrNull()
        }.let {
            if (it == null) {
                RootConfig.Welcome
            } else {
                RootConfig.Home
            }
        },
        childFactory = ::createScreenComponent
    )

    private fun createScreenComponent(
        rootConfig: RootConfig,
        componentContext: ComponentContext
    ): Component {
        return when (rootConfig) {
            is RootConfig.Welcome -> WelcomeScreenComponent(
                componentContext = componentContext,
                di = di,
                onHome = {
                    navigation.replaceAll(RootConfig.Home)
                }
            )
            is RootConfig.Home -> HomeScreenComponent(
                componentContext = componentContext,
                di = di,
                onMedium = {
                    navigation.bringToFront(RootConfig.Medium(it))
                }
            )
            is RootConfig.Medium -> MediumScreenComponent(
                componentContext = componentContext,
                di = di,
                initialSeriesData = rootConfig.seriesData
            )
        }
    }

    @OptIn(ExperimentalDecomposeApi::class)
    @Composable
    override fun render() {
        onRender {
            Children(
                stack = stack,
                animation = predictiveBackAnimation(
                    backHandler = this.backHandler,
                    fallbackAnimation = stackAnimation(fade()),
                    onBack = {
                        navigation.pop()
                    }
                )
            ) {
                it.instance.render()
            }
        }
    }
}