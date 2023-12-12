package dev.datlag.burningseries.shared.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.common.backAnimation
import dev.datlag.burningseries.shared.ui.screen.initial.InitialScreenComponent
import dev.datlag.burningseries.shared.ui.screen.video.VideoScreenComponent
import org.kodein.di.DI

class NavHostComponent(
    componentContext: ComponentContext,
    override val di: DI
) : Component, ComponentContext by componentContext {

    private val navigation = StackNavigation<ScreenConfig>()
    private val stack = childStack(
        source = navigation,
        serializer = ScreenConfig.serializer(),
        initialConfiguration = ScreenConfig.Home,
        childFactory = ::createScreenComponent
    )

    private fun createScreenComponent(
        screenConfig: ScreenConfig,
        componentContext: ComponentContext
    ) : Component {
        return when (screenConfig) {
            is ScreenConfig.Home -> InitialScreenComponent(
                componentContext = componentContext,
                di = di,
                watchVideo = { schemeKey, series, episode, stream ->
                    navigation.push(
                        ScreenConfig.Video(
                            schemeKey,
                            series,
                            episode,
                            stream.toList()
                        )
                    )
                }
            )
            is ScreenConfig.Video -> VideoScreenComponent(
                componentContext = componentContext,
                di = di,
                schemeKey = screenConfig.schemeKey,
                series = screenConfig.series,
                initialEpisode = screenConfig.episode,
                initialStreams = screenConfig.streams,
                onBack = navigation::pop
            )
        }
    }

    @Composable
    override fun render() {
        Children(
            stack = stack,
            animation = backAnimation(
                backHandler = this.backHandler,
                onBack = {
                    navigation.pop()
                }
            )
        ) {
            CompositionLocalProvider(
                LocalDI provides di
            ) {
                it.instance.render()
            }
        }
    }
}