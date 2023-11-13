package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.state.SearchAction
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStateMachine(
    private val client: HttpClient
) : FlowReduxStateMachine<SearchState, SearchAction>(initialState = SearchState.Loading) {
    init {
        spec {
            inState<SearchState.Loading> {
                onEnter { state ->
                    try {
                        val loadedGenres = BurningSeries.getSearch(client)
                        if (loadedGenres.isEmpty()) {
                            state.override { SearchState.Error(String()) }
                        } else {
                            state.override { SearchState.Success(loadedGenres) }
                        }
                    } catch (t: Throwable) {
                        state.override { SearchState.Error(t.message ?: String()) }
                    }
                }
            }

            inState<SearchState.Error> {
                on<SearchAction.Retry> { _, state ->
                    state.override { SearchState.Loading }
                }
            }
        }
    }
}