package dev.datlag.burningseries.shared.ui.screen.initial.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.state.HomeAction
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.series.SeriesScreenComponent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit,
    private val scrollEnabled: (Boolean) -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeStateMachine: HomeStateMachine by di.instance()
    override val homeState: StateFlow<HomeState> = homeStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), HomeState.Loading)

    private val navigation = SlotNavigation<HomeConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        handleBackButton = false
    ) { config, context ->
        when (config) {
            is HomeConfig.Series -> SeriesScreenComponent(
                componentContext = context,
                di = di,
                initialTitle = config.title,
                initialHref = config.href,
                initialCoverHref = config.coverHref,
                onGoBack = {
                    navigation.dismiss(scrollEnabled)
                },
                watchVideo = { schemeKey, series, episode, stream ->
                    watchVideo(schemeKey, series, episode, stream)
                }
            )
        }
    }

    @Composable
    override fun render() {
        HomeScreen(this)
    }

    override fun retryLoadingHome(): Any? = ioScope().launchIO {
        homeStateMachine.dispatch(HomeAction.Retry)
    }

    override fun itemClicked(config: HomeConfig) {
        navigation.activate(config) {
            scrollEnabled(false)
        }
    }
}