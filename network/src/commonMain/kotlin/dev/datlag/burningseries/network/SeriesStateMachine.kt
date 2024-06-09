package dev.datlag.burningseries.network

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.network.state.SeriesAction
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesStateMachine(
    private val client: HttpClient
) : FlowReduxStateMachine<SeriesState, SeriesAction>(
    initialState = SeriesState.Loading
) {

    var currentState: SeriesState = SeriesState.Loading
        private set

    private val seriesHref: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        spec {
            inState<SeriesState> {
                onEnterEffect {
                    currentState = it
                }
                collectWhileInState(seriesHref) { href, state ->
                    if (href.isNullOrBlank()) {
                        return@collectWhileInState state.override { SeriesState.Loading }
                    }

                    val result = suspendCatching {
                        BurningSeries.series(client, href)
                    }

                    state.override { SeriesState.fromResult(result) }
                }
            }
            inState<SeriesState.Failure> {
                onActionEffect<SeriesAction.Retry> { _, _ ->
                    val previous = seriesHref.getAndUpdate { null }
                    seriesHref.update { previous }
                }
            }
        }
    }

    fun href(value: String) {
        seriesHref.update { value }
    }
}