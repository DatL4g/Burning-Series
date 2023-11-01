package dev.datlag.burningseries.ui.screen.initial.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.state.HomeAction
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.screen.initial.series.SeriesScreenComponent
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : HomeComponent, ComponentContext by componentContext {

    private val homeStateMachine: HomeStateMachine by di.instance()
    override val homeState: StateFlow<HomeState> = homeStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, HomeState.Loading)

    private val navigation = SlotNavigation<HomeConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        handleBackButton = true
    ) { config, context ->
        when (config) {
            is HomeConfig.Series -> SeriesScreenComponent(context, di)
        }
    }

    @Composable
    override fun render() {
        HomeScreen(this)
    }

    override fun retryLoadingHome(): Any? = ioScope().launchIO {
        homeStateMachine.dispatch(HomeAction.Retry)
    }
}