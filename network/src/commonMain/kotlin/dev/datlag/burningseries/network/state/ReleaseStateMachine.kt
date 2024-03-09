package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.common.suspendCatchResult
import dev.datlag.burningseries.model.state.ReleaseState
import dev.datlag.burningseries.network.GitHub
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseStateMachine(
    private val gitHub: GitHub
) : FlowReduxStateMachine<ReleaseState, Nothing>(initialState = currentState) {
    init {
        spec {
            inState<ReleaseState.Loading> {
                onEnterEffect {
                    currentState = it
                }
                onEnter { state ->
                    NetworkStateSaver.Cache.release.getAlive()?.let {
                        return@onEnter state.override { ReleaseState.Success(it) }
                    }

                    val result = suspendCatchResult {
                        val releases = gitHub.getReleases(owner = "DatL4g", repo = "Burning-Series")
                        val filtered = releases.filterNot { it.draft || it.preRelease }

                        if (filtered.isEmpty()) {
                            ReleaseState.Error
                        } else {
                            ReleaseState.Success(filtered.also {
                                NetworkStateSaver.Cache.release.cache(it)
                            })
                        }
                    }.asSuccess {
                        NetworkStateSaver.Cache.release.getUnAlive()?.let {
                            ReleaseState.Success(it)
                        } ?: ReleaseState.Error
                    }

                    state.override { result }
                }
            }
            inState<ReleaseState.Success> {
                onEnterEffect {
                    currentState = it
                }
            }
            inState<ReleaseState.Error> {
                onEnterEffect {
                    currentState = it
                }
            }
        }
    }

    companion object {
        var currentState: ReleaseState
            set(value) {
                NetworkStateSaver.initialReleaseState = value
            }
            get() = NetworkStateSaver.initialReleaseState
    }
}