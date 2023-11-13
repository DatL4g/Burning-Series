package dev.datlag.burningseries.ui.screen.initial.search

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.common.ioDispatcher
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.network.state.SearchStateMachine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance

class SearchScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : SearchComponent, ComponentContext by componentContext {

    private val homeStateMachine: SearchStateMachine by di.instance()
    override val searchState: StateFlow<SearchState> = homeStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, SearchState.Loading)

    @Composable
    override fun render() {
        SearchScreen(this)
    }
}