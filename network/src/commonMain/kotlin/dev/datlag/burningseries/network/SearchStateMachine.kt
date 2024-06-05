package dev.datlag.burningseries.network

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.network.state.SearchAction
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.tooling.async.suspendCatching
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStateMachine(
    private val client: HttpClient
) : FlowReduxStateMachine<SearchState, SearchAction>(
    initialState = currentState
) {

    var currentState: SearchState
        get() = Companion.currentState
        private set(value) {
            Companion.currentState = value
        }

    init {
        spec {
            inState<SearchState> {
                onEnterEffect {
                    currentState = it
                }
            }
            inState<SearchState.Loading> {
                onEnter { state ->
                    val result = suspendCatching {
                        BurningSeries.search(client)
                    }

                    state.override {
                        SearchState.fromResult(result)
                    }
                }
            }
            inState<SearchState.Failure> {
                onEnterEffect {
                    Napier.e("BS Search Error:", it.throwable)
                    // log in crashlytics
                }
                on<SearchAction.Retry> { _, state ->
                    state.override { SearchState.Loading }
                }
            }
        }
    }

    companion object {
        var currentState: SearchState
            get() = StateSaver.searchState
            private set(value) {
                StateSaver.searchState = value
            }
    }
}