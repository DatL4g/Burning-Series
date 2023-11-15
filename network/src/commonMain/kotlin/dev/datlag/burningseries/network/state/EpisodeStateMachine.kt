package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.algorithm.MD5
import dev.datlag.burningseries.model.state.EpisodeAction
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.network.JsonBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeStateMachine(
    private val jsonBase: JsonBase
) : FlowReduxStateMachine<EpisodeState, EpisodeAction>(initialState = EpisodeState.Waiting) {
    init {
        spec {
            inState<EpisodeState.Waiting> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.Loading> {
                onEnter { state ->
                    val hosterHref = state.snapshot.episode.hosters.map { it.href }
                    val jsonBaseResults = coroutineScope {
                        hosterHref.map { async {
                            try {
                                val entry = jsonBase.burningSeriesCaptcha(MD5.hexString(it))
                                if (!entry.broken) {
                                    entry.url
                                } else {
                                    null
                                }
                            } catch (ignored: Throwable) {
                                null
                            }
                        } }.awaitAll().filterNotNull()
                    }
                    if (jsonBaseResults.isNotEmpty()) {
                        state.override { EpisodeState.Success(jsonBaseResults) }
                    } else {
                        state.override { EpisodeState.Error(String()) }
                    }
                }
            }

            inState<EpisodeState.Error> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }
        }
    }
}