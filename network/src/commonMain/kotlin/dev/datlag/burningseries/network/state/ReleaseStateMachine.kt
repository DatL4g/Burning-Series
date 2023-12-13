package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.state.ReleaseState
import dev.datlag.burningseries.network.GitHub
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseStateMachine(
    private val gitHub: GitHub
) : FlowReduxStateMachine<ReleaseState, Nothing>(initialState = NetworkStateSaver.initialReleaseState) {
    init {
        spec {
            inState<ReleaseState.Loading> {
                onEnterEffect {
                    NetworkStateSaver.initialReleaseState = it
                }
                onEnter { state ->
                    try {
                        val releases = gitHub.getReleases(owner = "DatL4g", repo = "Burning-Series")
                        val filtered = releases.filterNot { it.draft || it.preRelease }

                        if (filtered.isEmpty()) {
                            state.override { ReleaseState.Error }
                        } else {
                            state.override { ReleaseState.Success(filtered) }
                        }
                    } catch (t: Throwable) {
                        state.override { ReleaseState.Error }
                    }
                }
            }
            inState<ReleaseState.Success> {
                onEnterEffect {
                    NetworkStateSaver.initialReleaseState = it
                }
            }
            inState<ReleaseState.Error> {
                onEnterEffect {
                    NetworkStateSaver.initialReleaseState = it
                }
            }
        }
    }
}