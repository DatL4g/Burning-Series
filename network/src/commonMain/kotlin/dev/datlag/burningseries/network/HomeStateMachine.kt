package dev.datlag.burningseries.network

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.network.state.HomeAction
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.tooling.async.suspendCatching
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateMachine(
    private val client: HttpClient
) : FlowReduxStateMachine<HomeState, HomeAction>(
    initialState = currentState
) {

    var currentState: HomeState
        get() = Companion.currentState
        private set(value) {
            Companion.currentState = value
        }

    init {
        spec {
            inState<HomeState> {
                onEnterEffect {
                    currentState = it
                }
            }
            inState<HomeState.Loading> {
                onEnter { state ->
                    val result = suspendCatching {
                        BurningSeries.home(client)
                    }

                    state.override {
                        HomeState.fromResult(result)
                    }
                }
            }
            inState<HomeState.Failure> {
                onEnterEffect {
                    Napier.e("BS Home Error:", it.throwable)
                    // log in crashlytics
                }
                on<HomeAction.Retry> { _, state ->
                    state.override { HomeState.Loading }
                }
            }
        }
    }

    companion object {
        var currentState: HomeState
            get() = StateSaver.homeState
            private set(value) {
                StateSaver.homeState = value
            }
    }
}