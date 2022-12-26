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
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.home.episode.EpisodesViewComponent
import dev.datlag.burningseries.ui.screen.home.series.SeriesViewComponent
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.io.InputStream

class HomeScreenComponent(
    componentContext: ComponentContext,
    private val onSearch: () -> Unit,
    override val onEpisodeClicked: (String, SeriesInitialInfo, Boolean) -> Unit,
    override val onSeriesClicked: (String, SeriesInitialInfo) -> Unit,
    override val onSettingsClicked: () -> Unit,
    override val onAboutClicked: () -> Unit,
    override val di: DI
) : HomeComponent, ComponentContext by componentContext {

    private val dialogNavigation = OverlayNavigation<DialogConfig>()

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

    init {
        childIndex.observe(lifecycle) {
            if (it == 0) {
                navigation.replaceCurrent(View.Episode)
            } else {
                navigation.replaceCurrent(View.Series)
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

    override fun onSearchClicked() {
        onSearch()
    }

    @Parcelize
    private data class DialogConfig(
        val message: String
    ): Parcelable

    @Parcelize
    sealed class View : Parcelable {
        object Episode : View()
        object Series : View()
    }
}