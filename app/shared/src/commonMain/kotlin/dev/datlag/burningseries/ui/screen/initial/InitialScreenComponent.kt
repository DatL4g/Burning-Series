package dev.datlag.burningseries.ui.screen.initial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.state.HomeAction
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.initial.favorite.FavoriteScreenComponent
import dev.datlag.burningseries.ui.screen.initial.home.HomeScreenComponent
import dev.datlag.burningseries.ui.screen.initial.search.SearchScreenComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.kodein.di.DI
import org.kodein.di.instance

class InitialScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
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
                componentContext,
                di
            )
            is View.Favorite -> FavoriteScreenComponent(
                componentContext,
                di
            )
            is View.Search -> SearchScreenComponent(
                componentContext,
                di
            )
        }
    }

    @OptIn(ExperimentalDecomposeApi::class)
    override fun selectPage(index: Int) {
        pagesNavigation.select(index = index)
    }
}