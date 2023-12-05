package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.model.state.SearchAction
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.network.WrapAPI
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStateMachine(
    private val client: HttpClient,
    private val json: Json,
    private val wrapApi: WrapAPI,
    private val wrapApiKey: String?,
    private val saveToDB: (SearchState.Success) -> Unit,
    private val loadFromDB: suspend () -> List<Genre>
) : FlowReduxStateMachine<SearchState, SearchAction>(initialState = SearchState.Loading) {
    init {
        spec {
            inState<SearchState.Loading> {
                onEnter { state ->
                    try {
                        val loadedClient = suspendCatching {
                            BurningSeries.getSearch(client)
                        }.getOrNull() ?: emptyList()

                        val loadedWeb = loadedClient.ifEmpty {
                            suspendCatching {
                                wrapApiKey?.let {
                                    val response = wrapApi.getBurningSeriesSearch(it)

                                    suspendCatching {
                                        json.decodeFromJsonElement<List<Genre>>(response.data)
                                    }.getOrNull() ?: suspendCatching {
                                        response.data.jsonObject["genre"]?.let { j -> json.decodeFromJsonElement<List<Genre>>(j) }
                                    }.getOrNull()
                                }
                            }.getOrNull() ?: emptyList()
                        }

                        val loadedGenres = loadedWeb.ifEmpty {
                            loadFromDB()
                        }
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

            inState<SearchState.Success> {
                onEnterEffect {
                    saveToDB(it)
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