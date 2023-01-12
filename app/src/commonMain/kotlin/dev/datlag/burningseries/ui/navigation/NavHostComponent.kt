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
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.datastore.common.showedLogin
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.ui.screen.about.AboutScreenComponent
import dev.datlag.burningseries.ui.screen.activate.ActivateScreenComponent
import dev.datlag.burningseries.ui.screen.favorite.FavoriteScreenComponent
import dev.datlag.burningseries.ui.screen.genre.GenreScreenComponent
import dev.datlag.burningseries.ui.screen.home.HomeScreenComponent
import dev.datlag.burningseries.ui.screen.login.LoginScreenComponent
import dev.datlag.burningseries.ui.screen.series.SeriesScreenComponent
import dev.datlag.burningseries.ui.screen.settings.SettingsScreenComponent
import dev.datlag.burningseries.ui.screen.video.VideoScreenComponent
import io.ktor.client.plugins.cookies.*
import org.kodein.di.*
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.repository.GitHubRepository
import dev.datlag.burningseries.other.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import dev.datlag.burningseries.other.Resources

class NavHostComponent private constructor(
    componentContext: ComponentContext,
    override val di: DI
) : Component, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val navigation = StackNavigation<ScreenConfig>()
    private val userDataStore: DataStore<UserSettings> by di.instance()
    private val githubRepo: GitHubRepository by di.instance()

    private val stack = childStack(
        source = navigation,
        initialStack = {
            val showedLogin = userDataStore.showedLogin.getValueBlocking(false)
            val defaultScreen = if (showedLogin) {
                ScreenConfig.Home
            } else {
                ScreenConfig.Home // Login, maybe find some workaround for session
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
                ::onFavoritesClicked,
                ::onSearchClicked,
                ::onEpisodeClicked,
                ::onSeriesClicked,
                ::onSettingsClicked,
                ::onAboutClicked,
                di
            )
        }
        return when (screenConfig) {
            is ScreenConfig.Login -> LoginScreenComponent(
                componentContext,
                ::onLoginClicked,
                ::onLoginSkipClicked,
                di
            )
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
                screenConfig.isEpisode,
                screenConfig.continueWatching,
                ::onGoBackClicked,
                ::onEpisodeClicked,
                ::onActivateClicked,
                ::onSettingsClicked,
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
                ::onWatchClicked,
                di
            )
            is ScreenConfig.About -> AboutScreenComponent(
                componentContext,
                ::onGoBackClicked,
                di
            )
            is ScreenConfig.Settings -> SettingsScreenComponent(
                componentContext,
                ::onGoBackClicked,
                di
            )
            is ScreenConfig.Favorites -> FavoriteScreenComponent(
                componentContext,
                ::onGoBackClicked,
                ::onSeriesClicked,
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

    private fun onLoginSkipClicked() {
        navigation.push(ScreenConfig.Home)
    }

    private fun onFavoritesClicked() {
        navigation.push(ScreenConfig.Favorites)
    }

    private fun onSearchClicked() {
        navigation.push(ScreenConfig.Genre)
    }

    private fun onEpisodeClicked(
        href: String,
        initialInfo: SeriesInitialInfo,
        continueWatching: Boolean
    ) {
        navigation.push(ScreenConfig.Series(href, initialInfo, true, continueWatching))
    }

    private fun onSeriesClicked(
        href: String,
        initialInfo: SeriesInitialInfo
    ) {
        navigation.push(ScreenConfig.Series(href, initialInfo, isEpisode = false, continueWatching = false))
    }

    private fun onEpisodeClicked(series: Series, episode: Series.Episode, streams: List<VideoStream>) {
        navigation.push(ScreenConfig.Video(series, episode, streams))
    }

    private fun onActivateClicked(series: Series, episode: Series.Episode) {
        navigation.push(ScreenConfig.Activate(series, episode))
    }

    private fun onSettingsClicked() {
        navigation.push(ScreenConfig.Settings)
    }

    private fun onAboutClicked() {
        navigation.push(ScreenConfig.About)
    }

    private fun onWatchClicked(series: Series, episode: Series.Episode, stream: VideoStream) {
        navigation.push(ScreenConfig.Video(series, episode, listOf(stream)))
    }

    init {
        BackPressedListener = {
            navigation.pop(onComplete = { isSuccess ->
                NavigationListener?.invoke(!isSuccess)
            })
        }
        scope.launch(Dispatchers.IO) {
            var loadedNewRepo = false
            val installed = Resources.version
            githubRepo.loadReleases(
                installed,
                Constants.GITHUB_OWNER,
                Constants.GITHUB_REPO_OLD
            )

            githubRepo.status.collect {
                if (!loadedNewRepo && it is Status.ERROR) {
                    githubRepo.loadReleases(
                        installed,
                        Constants.GITHUB_OWNER,
                        Constants.GITHUB_REPO_NEW
                    )
                    loadedNewRepo = true
                }
            }
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