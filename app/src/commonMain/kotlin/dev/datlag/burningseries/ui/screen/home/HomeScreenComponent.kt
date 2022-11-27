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
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.dialog.example.ExampleDialogComponent
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.home.episode.EpisodesViewComponent
import dev.datlag.burningseries.ui.screen.home.series.SeriesViewComponent
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI
import org.kodein.di.DIAware

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : HomeComponent, ComponentContext by componentContext {

    private val dialogNavigation = OverlayNavigation<DialogConfig>()
    private val _dialog = childOverlay(
        source = dialogNavigation,
        handleBackButton = true
    ) { config, componentContext ->
        ExampleDialogComponent(
            componentContext = componentContext,
            message = config.message,
            onDismissed = dialogNavigation::dismiss
        )
    }
    override val dialog: Value<ChildOverlay<*, DialogComponent>> = _dialog

    private val navigation = StackNavigation<View>()
    private val _childStack = childStack(
        source = navigation,
        initialConfiguration = View.Episode,
        handleBackButton = true,
        childFactory = ::createChild
    )
    override val childStack: Value<ChildStack<*, Component>> = _childStack
    override val childIndex: MutableValue<Int> = MutableValue(0)

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
            is View.Episode -> EpisodesViewComponent(componentContext, di)
            is View.Series -> SeriesViewComponent(componentContext, di)
        }
    }

    @Composable
    override fun render() {
        HomeScreen(this)
    }

    override fun showDialog(message: String) {
        dialogNavigation.activate(DialogConfig(message = message))
    }

    @Parcelize
    private data class DialogConfig(
        val message: String
    ): Parcelable

    @Parcelize
    private sealed class View : Parcelable {
        object Episode : View()
        object Series : View()
    }
}