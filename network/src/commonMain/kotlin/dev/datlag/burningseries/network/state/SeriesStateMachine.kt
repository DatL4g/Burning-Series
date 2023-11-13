package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesStateMachine(
    private val client: HttpClient,
    private val href: String
) : FlowReduxStateMachine<SeriesState, SeriesAction>(SeriesState.Loading(href)) {

    init {
        spec {
            inState<SeriesState.Loading> {
                onEnter { state ->
                    try {
                        val result = BurningSeries.getSeries(client, state.snapshot.href)!!
                        state.override { SeriesState.Success(result) }
                    } catch (e: Throwable) {
                        state.override { SeriesState.Error(e.message ?: String()) }
                    }
                }
                on<SeriesAction.Load> { action, state ->
                    state.mutate {
                        this.copy(href = action.href)
                    }
                }
            }

            inState<SeriesState.Error> {
                on<SeriesAction.Retry> { _, state ->
                    state.override { SeriesState.Loading(href) }
                }
                on<SeriesAction.Load> { action, state ->
                    state.override { SeriesState.Loading(action.href) }
                }
            }

            inState<SeriesState.Success> {
                on<SeriesAction.Load> { action, state ->
                    state.override { SeriesState.Loading(action.href) }
                }
            }
        }
    }
}