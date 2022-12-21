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
import dev.datlag.burningseries.BackPressedListener
import dev.datlag.burningseries.NavigationListener
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.datastore.common.showedLogin
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.ui.screen.activate.ActivateScreenComponent
import dev.datlag.burningseries.ui.screen.genre.GenreScreenComponent
import dev.datlag.burningseries.ui.screen.home.HomeScreenComponent
import dev.datlag.burningseries.ui.screen.login.LoginScreenComponent
import dev.datlag.burningseries.ui.screen.series.SeriesScreenComponent
import dev.datlag.burningseries.ui.screen.video.VideoScreenComponent
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
        val homeConfig by lazy(LazyThreadSafetyMode.NONE) {
            HomeScreenComponent(
                componentContext,
                ::onSearchClicked,
                ::onSeriesClicked,
                di
            )
        }
        return when (screenConfig) {
            is ScreenConfig.Login -> LoginScreenComponent(componentContext, ::onLoginClicked, di)
            is ScreenConfig.Home -> homeConfig
            is ScreenConfig.Genre -> GenreScreenComponent(
                componentContext,
                ::onGoBackClicked,
                ::onSeriesClicked,
                di
            )
            is ScreenConfig.Series -> SeriesScreenComponent(
                componentContext,
                screenConfig.href,
                screenConfig.initialInfo,
                ::onGoBackClicked,
                ::onEpisodeClicked,
                ::onActivateClicked,
                di
            )
            is ScreenConfig.Video -> VideoScreenComponent(
                componentContext,
                screenConfig.series,
                screenConfig.episode,
                screenConfig.streams,
                ::onGoBackClicked,
                di
            )
            is ScreenConfig.Activate -> ActivateScreenComponent(
                componentContext,
                screenConfig.series,
                screenConfig.episode,
                ::onGoBackClicked,
                di
            )
            else -> homeConfig
        }
    }

    private fun onGoBackClicked() {
        navigation.pop()
    }

    private fun onLoginClicked() {
        navigation.push(ScreenConfig.Home)
    }

    private fun onSearchClicked() {
        navigation.push(ScreenConfig.Genre)
    }

    private fun onSeriesClicked(
        href: String,
        initialInfo: SeriesInitialInfo
    ) {
        navigation.push(ScreenConfig.Series(href, initialInfo))
    }

    private fun onEpisodeClicked(series: Series, episode: Series.Episode, streams: List<VideoStream>) {
        navigation.push(ScreenConfig.Video(series, episode, streams))
    }

    private fun onActivateClicked(series: Series, episode: Series.Episode) {
        navigation.push(ScreenConfig.Activate(series, episode))
    }

    init {
        BackPressedListener = {
            navigation.pop(onComplete = { isSuccess ->
                NavigationListener?.invoke(!isSuccess)
            })
        }
    }

    @OptIn(ExperimentalDecomposeApi::class)
    @Composable
    override fun render() {
        Children(
            stack = stack,
            animation = stackAnimation(fade())
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