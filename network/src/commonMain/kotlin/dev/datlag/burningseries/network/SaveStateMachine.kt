package dev.datlag.burningseries.network

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.firebase.FirebaseFactory
import dev.datlag.burningseries.network.state.SaveAction
import dev.datlag.burningseries.network.state.SaveState
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class SaveStateMachine(
    private val client: HttpClient,
    private val streamClient: HttpClient?,
    private val firebaseAuth: FirebaseFactory.Auth?,
    private val fireStore: FirebaseFactory.Store?
) : FlowReduxStateMachine<SaveState, SaveAction>(
    initialState = SaveState.None
) {

    init {
        spec {
            inState<SaveState> {
                onEnterEffect {
                    if (firebaseAuth?.isSignedIn != true) {
                        firebaseAuth?.signInAnonymously()
                    }
                }
                on<SaveAction.Clear> { _, state ->
                    state.override { SaveState.None }
                }
            }
            inState<SaveState.None> {
                on<SaveAction.Save> { action, state ->
                    state.override { SaveState.Saving(action.episodeHref, action.data) }
                }
            }
            inState<SaveState.Saving> {
                onEnter { state ->
                    val firebaseSaved = suspendCatching {
                        fireStore?.addStream(
                            data = state.snapshot.data.fireStore
                        )
                    }.getOrNull() ?: false

                    val series = suspendCatching {
                        state.snapshot.episodeHref?.let { href ->
                            BurningSeries.series(client, href)
                        }
                    }.getOrNull()

                    val episode = state.snapshot.episodeHref?.let { href ->
                        series?.episodes?.firstOrNull {
                            it.href == href
                        } ?: series?.episodes?.firstOrNull {
                            it.href.equals(href, ignoreCase = true)
                        }
                    }

                    val stream = suspendCatching {
                        Skeo.loadVideos(streamClient ?: client, state.snapshot.data.url)
                    }.getOrNull().orEmpty().toImmutableSet()

                    state.override {
                        if (firebaseSaved) {
                            SaveState.Success(series, episode, stream)
                        } else {
                            SaveState.Error(series, episode, stream)
                        }
                    }
                }
            }
            inState<SaveState.Success> {
                on<SaveAction.Save> { action, state ->
                    state.override { SaveState.Saving(action.episodeHref, action.data) }
                }
            }
            inState<SaveState.Error> {
                on<SaveAction.Save> { action, state ->
                    state.override { SaveState.Saving(action.episodeHref, action.data) }
                }
            }
        }
    }
}