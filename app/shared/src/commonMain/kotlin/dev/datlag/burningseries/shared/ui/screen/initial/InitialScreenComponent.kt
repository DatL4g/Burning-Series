package dev.datlag.burningseries.shared.ui.screen.initial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Shortcut
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.favorite.FavoriteScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.sponsor.SponsorScreenComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI

class InitialScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    shortcutIntent: Shortcut.Intent,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit,
    private val onBack: () -> Unit
) : InitialComponent, ComponentContext by componentContext {

    override val pagerItems: List<InitialComponent.PagerItem> = listOf(
        InitialComponent.PagerItem(
            label = SharedRes.strings.sponsor,
            unselectedIcon = Icons.Outlined.Savings,
            selectedIcon = Icons.Filled.Savings
        ),
        InitialComponent.PagerItem(
            label = SharedRes.strings.home,
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        InitialComponent.PagerItem(
            label = SharedRes.strings.favorites,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
            selectedIcon = Icons.Filled.Favorite
        )
    )

    @OptIn(ExperimentalDecomposeApi::class)
    private val pagesNavigation = PagesNavigation<View>()

    @OptIn(ExperimentalDecomposeApi::class)
    override val pages: Value<ChildPages<View, Component>> = childPages(
        source = pagesNavigation,
        serializer = View.serializer(),
        initialPages = {
            Pages(
                items = listOf(
                    View.Sponsor,
                    View.Home(shortcutIntent),
                    View.Favorite
                ),
                selectedIndex = when (shortcutIntent) {
                    is Shortcut.Intent.SEARCH -> 1
                    is Shortcut.Intent.Series -> 1
                    else -> 1
                }
            )
        }
    ) { config, context ->
        createChild(config, context)
    }

    @OptIn(ExperimentalDecomposeApi::class)
    override val selectedPage: Value<Int> = pages.map { it.selectedIndex }

    override val sponsorScrollEnabled = MutableStateFlow(true)
    override val homeScrollEnabled = MutableStateFlow(true)
    override val favoriteScrollEnabled = MutableStateFlow(true)

    private val backCallback = BackCallback {
        pageBack()
    }

    init {
        backHandler.register(backCallback)
    }

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            InitialScreen(this)
        }
    }

    private fun createChild(
        view: View,
        componentContext: ComponentContext
    ) : Component {
        return when (view) {
            is View.Sponsor -> SponsorScreenComponent(
                componentContext = componentContext,
                di = di
            )
            is View.Home -> HomeScreenComponent(
                componentContext = componentContext,
                di = di,
                shortcutIntent = view.shortcutIntent,
                watchVideo = { schemeKey, series, episode, stream ->
                    watchVideo(schemeKey, series, episode, stream)
                },
                scrollEnabled = { homeScrollEnabled.value = it }
            )
            is View.Favorite -> FavoriteScreenComponent(
                componentContext = componentContext,
                di = di,
                watchVideo = { schemeKey, series, episode, stream ->
                    watchVideo(schemeKey, series, episode, stream)
                },
                scrollEnabled = { favoriteScrollEnabled.value = it }
            )
        }
    }

    @OptIn(ExperimentalDecomposeApi::class)
    override fun selectPage(index: Int) {
        pagesNavigation.select(index = index) { new, old ->
            if (new.items[new.selectedIndex] == old.items[old.selectedIndex]) {
                pageBack()
            }
        }
    }

    @OptIn(ExperimentalDecomposeApi::class)
    private fun pageBack() {
        (pages.value.items[pages.value.selectedIndex].instance as? SeriesHolderComponent)
                ?.dismissHoldingSeries()
                ?: onBack()
    }
}