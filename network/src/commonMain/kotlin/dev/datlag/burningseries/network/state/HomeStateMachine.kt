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
) : FlowReduxStateMachine<HomeState, HomeAction>(initialState = currentState) {
    init {
        spec {
            inState<HomeState.Loading> {
                onEnterEffect {
                    currentState = it
                }
                onEnter { state ->
                    NetworkStateSaver.Cache.home.getAlive()?.let {
                        return@onEnter state.override { HomeState.Success(it.first, it.second) }
                    }

                    val result = suspendCatchResult {
                        var onDeviceReachable: Boolean = true
                        val loadedHome = BurningSeries.getHome(client) ?: run {
                            onDeviceReachable = false
                            wrapApiKey?.let {
                                json.decodeFromJsonElement<Home>(wrapApi.getBurningSeriesHome(it).data)
                            }
                        }!!

                        if (loadedHome.episodes.isNotEmpty() || loadedHome.series.isNotEmpty()) {
                            NetworkStateSaver.Cache.home.cache(loadedHome to onDeviceReachable)
                            HomeState.Success(loadedHome, onDeviceReachable)
                        } else {
                            HomeState.Error
                        }
                    }
                    result.onError {
                        Napier.e("HomeStateError", it)
                    }
                    state.override {
                        result.asSuccess {
                            NetworkStateSaver.Cache.home.getUnAlive()?.let {
                                HomeState.Success(it.first, it.second)
                            } ?: HomeState.Error
                        }
                    }
                }
            }
            inState<HomeState.Success> {
                onEnterEffect {
                    currentState = it
                }
            }
            inState<HomeState.Error> {
                onEnterEffect {
                    currentState = it
                }
                on<HomeAction.Retry> { _, state ->
                    state.override { HomeState.Loading }
                }
            }
        }
    }

    companion object {
        var currentState: HomeState
            set(value) {
                NetworkStateSaver.initialHomeState = value
            }
            get() = NetworkStateSaver.initialHomeState
    }
}