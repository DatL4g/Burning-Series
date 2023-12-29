package dev.datlag.burningseries.shared.ui.screen.initial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Shortcut
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.ui.screen.initial.favorite.FavoriteScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.search.SearchScreenComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI

class InitialScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    shortcutIntent: Shortcut.Intent,
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
    override val pages: Value<ChildPages<View, SeriesHolderComponent>> = childPages(
        source = pagesNavigation,
        serializer = View.serializer(),
        initialPages = {
            Pages(
                items = listOf(
                    View.Home(shortcutIntent),
                    View.Favorite,
                    View.Search
                ),
                selectedIndex = when (shortcutIntent) {
                    is Shortcut.Intent.SEARCH -> 2
                    is Shortcut.Intent.Series -> 0
                    else -> 0
                }
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
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            InitialScreen(this)
        }
    }

    private fun createChild(
        view: View,
        componentContext: ComponentContext
    ) : SeriesHolderComponent {
        return when (view) {
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
        pagesNavigation.select(index = index) { new, old ->
            if (new.items[new.selectedIndex] == old.items[old.selectedIndex]) {
                pages.value.items[pages.value.selectedIndex].instance?.dismissHoldingSeries()
            }
        }
    }
}