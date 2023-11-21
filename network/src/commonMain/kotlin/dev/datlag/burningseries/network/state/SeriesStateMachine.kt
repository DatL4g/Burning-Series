package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.WrapAPIResponse
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.WrapAPI
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesStateMachine(
    private val client: HttpClient,
    private val href: String,
    private val json: Json,
    private val wrapAPI: WrapAPI,
    private val wrapAPIKey: String?
) : FlowReduxStateMachine<SeriesState, SeriesAction>(SeriesState.Loading(href)) {

    init {
        spec {
            inState<SeriesState.Loading> {
                onEnter { state ->
                    try {
                        var onDeviceReachable: Boolean = true
                        val result = BurningSeries.getSeries(client, state.snapshot.href) ?: run {
                            onDeviceReachable = false
                            wrapAPIKey?.let {
                                val wrapResult = wrapAPI.getBurningSeries(it, BSUtil.fixSeriesHref(state.snapshot.href))
                                json.decodeFromJsonElement<Series>(wrapResult.data)
                            }
                        }!!
                        state.override { SeriesState.Success(result, onDeviceReachable) }
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