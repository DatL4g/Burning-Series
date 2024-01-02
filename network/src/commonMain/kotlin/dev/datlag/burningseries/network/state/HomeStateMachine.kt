package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.common.suspendCatchResult
import dev.datlag.burningseries.model.state.HomeAction
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.network.WrapAPI
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateMachine(
    private val client: HttpClient,
    private val json: Json,
    private val wrapApi: WrapAPI,
    private val wrapApiKey: String?
) : FlowReduxStateMachine<HomeState, HomeAction>(initialState = NetworkStateSaver.initialHomeState) {
    init {
        spec {
            inState<HomeState.Loading> {
                onEnterEffect {
                    NetworkStateSaver.initialHomeState = it
                }
                onEnter { state ->
                    val result = suspendCatchResult {
                        val loadedHome = BurningSeries.getHome(client) ?: run {
                            wrapApiKey?.let {
                                json.decodeFromJsonElement<Home>(wrapApi.getBurningSeriesHome(it).data)
                            }
                        }!!

                        if (loadedHome.episodes.isNotEmpty() || loadedHome.series.isNotEmpty()) {
                            HomeState.Success(loadedHome)
                        } else {
                            HomeState.Error
                        }
                    }
                    result.onError {
                        Napier.e("HomeStateError", it)
                    }
                    state.override { result.asSuccess { HomeState.Error } }
                }
            }
            inState<HomeState.Success> {
                onEnterEffect {
                    NetworkStateSaver.initialHomeState = it
                }
            }
            inState<HomeState.Error> {
                onEnterEffect {
                    NetworkStateSaver.initialHomeState = it
                }
                on<HomeAction.Retry> { _, state ->
                    state.override { HomeState.Loading }
                }
            }
        }
    }
}