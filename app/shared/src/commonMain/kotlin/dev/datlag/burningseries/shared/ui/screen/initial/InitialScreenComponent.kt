package dev.datlag.burningseries.shared.ui.screen.initial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.favorite.FavoriteScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.search.SearchScreenComponent
import kotlinx.coroutines.flow.*
import org.kodein.di.DI

class InitialScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit
) : InitialComponent, ComponentContext by componentContext {

    override val pagerItems: List<InitialComponent.PagerItem> = listOf(
        InitialComponent.PagerItem(
            SharedRes.strings.home,
            Icons.Default.Home
        ),
        InitialComponent.PagerItem(
            SharedRes.strings.favorites,
            Icons.Default.Favorite
        ),
        InitialComponent.PagerItem(
            SharedRes.strings.search,
            Icons.Default.Search
        )
    )

    @OptIn(ExperimentalDecomposeApi::class)
    private val pagesNavigation = PagesNavigation<View>()

    @OptIn(ExperimentalDecomposeApi::class)
    override val pages: Value<ChildPages<*, Component>> = childPages(
        source = pagesNavigation,
        initialPages = {
            Pages(
                items = listOf(
                    View.Home,
                    View.Favorite,
                    View.Search
                ),
                selectedIndex = 0
            )
        }
    ) { config, context ->
        createChild(config, context)
    }

    override val homeScrollEnabled = MutableStateFlow(true)
    override val favoriteScrollEnabled = MutableStateFlow(true)
    override val searchScrollEnabled = MutableStateFlow(true)

    @Composable
    override fun render() {
        InitialScreen(this)
    }

    private fun createChild(
        view: View,
        componentContext: ComponentContext
    ) : Component {
        return when (view) {
            is View.Home -> HomeScreenComponent(
                componentContext = componentContext,
                di = di,
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
            is View.Search -> SearchScreenComponent(
                componentContext = componentContext,
                di = di,
                watchVideo = { schemeKey, series, episode, stream ->
                    watchVideo(schemeKey, series, episode, stream)
                },
                scrollEnabled = { searchScrollEnabled.value = it }
            )
        }
    }

    @OptIn(ExperimentalDecomposeApi::class)
    override fun selectPage(index: Int) {
        pagesNavigation.select(index = index)
    }
}