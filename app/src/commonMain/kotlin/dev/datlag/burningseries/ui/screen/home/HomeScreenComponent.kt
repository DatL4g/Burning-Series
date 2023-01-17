package dev.datlag.burningseries.ui.screen.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.overlay.*
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.essenty.statekeeper.consume
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrDefault
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.repository.GitHubRepository
import dev.datlag.burningseries.network.repository.HomeRepository
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.dialog.release.NewReleaseDialogComponent
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.home.episode.EpisodesViewComponent
import dev.datlag.burningseries.ui.screen.home.series.SeriesViewComponent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import org.kodein.di.DI
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.safeEmit
import dev.datlag.burningseries.model.Home
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance

class HomeScreenComponent(
    componentContext: ComponentContext,
    private val onFavorites: () -> Unit,
    private val onSearch: () -> Unit,
    override val onEpisodeClicked: (String, SeriesInitialInfo, Boolean) -> Unit,
    override val onSeriesClicked: (String, SeriesInitialInfo) -> Unit,
    override val onSettingsClicked: () -> Unit,
    override val onAboutClicked: () -> Unit,
    override val di: DI
) : HomeComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val dialogNavigation = OverlayNavigation<DialogConfig>()
    private val _dialog = childOverlay(
        source = dialogNavigation,
        handleBackButton = true
    ) { config, componentContext ->
        when (config) {
            is DialogConfig.NewRelease -> NewReleaseDialogComponent(
                componentContext,
                config.release,
                onDismissed = dialogNavigation::dismiss,
                di = di
            ) as DialogComponent
        }
    }
    override val dialog: Value<ChildOverlay<DialogConfig, DialogComponent>> = _dialog

    private val navigation = StackNavigation<View>()
    private val _childStack = lazy {
        childStack(
            source = navigation,
            initialConfiguration = View.Episode,
            handleBackButton = true,
            childFactory = ::createChild
        )
    }
    override val childStack: Lazy<Value<ChildStack<View, Component>>> = _childStack
    override val childIndex: MutableValue<Int> = MutableValue(0)

    override val pagerList = lazy(LazyThreadSafetyMode.NONE) {
        listOf<Component>(
            EpisodesViewComponent(componentContext, onEpisodeClicked, di),
            SeriesViewComponent(componentContext, onSeriesClicked, di)
        )
    }

    private val homeRepo: HomeRepository by di.instance()
    private val db: BurningSeriesDB by di.instance()
    private val githubRepo: GitHubRepository by di.instance()

    override val status = homeRepo.status
    override val favoritesExists: Flow<Boolean> = db.burningSeriesQueries.favoritesExists().asFlow().mapToOneOrDefault(false)

    private val homeStateFlow: MutableStateFlow<Home> = homeRepo.homeState

    init {
        stateKeeper.consume<Home>(key = HOME_STATE)?.let {
            homeStateFlow.safeEmit(it, scope)
        }

        stateKeeper.register(key = HOME_STATE) {
            homeStateFlow.value
        }

        childIndex.observe(lifecycle) {
            if (it == 0) {
                navigation.replaceCurrent(View.Episode)
            } else {
                navigation.replaceCurrent(View.Series)
            }
        }

        scope.launch(Dispatchers.IO) {
            githubRepo.newRelease.mapNotNull { it }.collect {
                withContext(CommonDispatcher.Main) {
                    showDialog(DialogConfig.NewRelease(it))
                }
            }
        }
    }

    private fun createChild(view: View, componentContext: ComponentContext): Component {
        return when (view) {
            is View.Episode -> EpisodesViewComponent(
                componentContext,
                onEpisodeClicked,
                di
            )
            is View.Series -> SeriesViewComponent(
                componentContext,
                onSeriesClicked,
                di
            )
        }
    }

    @Composable
    override fun render() {
        HomeScreen(this)
    }

    override fun onFavoritesClicked() {
        onFavorites()
    }

    override fun onSearchClicked() {
        onSearch()
    }

    override fun showDialog(config: DialogConfig) {
        dialogNavigation.activate(config)
    }

    @Parcelize
    sealed class View : Parcelable {
        object Episode : View()
        object Series : View()
    }

    companion object {
        const val HOME_STATE = "HOME_STATE"
    }
}