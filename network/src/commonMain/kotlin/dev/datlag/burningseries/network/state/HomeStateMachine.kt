package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.model.state.HomeAction
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateMachine(
    private val client: HttpClient
) : FlowReduxStateMachine<HomeState, HomeAction>(initialState = HomeState.Loading) {
    init {
        spec {
            inState<HomeState.Loading> {
                onEnter { state ->
                    try {
                        val loadedHome = BurningSeries.getHome(client)!!
                        state.override { HomeState.Success(loadedHome) }
                    } catch (t: Throwable) {
                        state.override { HomeState.Error(t.message ?: String()) }
                    }
                }
            }

            inState<HomeState.Error> {
                on<HomeAction.Retry> { _, state ->
                    state.override { HomeState.Loading }
                }
            }
        }
    }
}