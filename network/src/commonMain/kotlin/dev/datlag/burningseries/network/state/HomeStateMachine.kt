package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.state.HomeAction
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.network.WrapAPI
import dev.datlag.burningseries.network.scraper.BurningSeries
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
) : FlowReduxStateMachine<HomeState, HomeAction>(initialState = HomeState.Loading) {
    init {
        spec {
            inState<HomeState.Loading> {
                onEnter { state ->
                    try {
                        val loadedHome = BurningSeries.getHome(client) ?: run {
                            wrapApiKey?.let {
                                json.decodeFromJsonElement<Home>(wrapApi.getBurningSeriesHome(it).data)
                            }
                        }!!

                        if (loadedHome.episodes.isNotEmpty() || loadedHome.series.isNotEmpty()) {
                            state.override { HomeState.Success(loadedHome) }
                        } else {
                            state.override { HomeState.Error(String()) }
                        }
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