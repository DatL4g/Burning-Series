package dev.datlag.burningseries.network

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.firebase.FirebaseFactory
import dev.datlag.burningseries.network.state.EpisodeAction
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeStateMachine(
    private val client: HttpClient,
    private val firebaseAuth: FirebaseFactory.Auth?,
    private val fireStore: FirebaseFactory.Store?,
    private val crashlytics: FirebaseFactory.Crashlytics?
) : FlowReduxStateMachine<EpisodeState, EpisodeAction>(
    initialState = EpisodeState.None
) {

    init {
        spec {
            inState<EpisodeState> {
                onEnterEffect {
                    if (firebaseAuth?.isSignedIn != true) {
                        firebaseAuth?.signInAnonymously()
                    }
                }
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
                on<EpisodeAction.LoadNonSuccess> { action, state ->
                    if (state.snapshot is EpisodeState.SuccessStream) {
                        state.noChange()
                    } else {
                        state.override { EpisodeState.Loading(action.episode) }
                    }
                }
                on<EpisodeAction.Clear> { _, state ->
                    state.override { EpisodeState.None }
                }
            }
            inState<EpisodeState.Loading> {
                onEnter { state ->
                    val hosterHref = state.snapshot.episode.hoster.map { it.href }

                    val firebaseResult = suspendCatching {
                        fireStore?.streams(hosterHref)
                    }
                    val lists = firebaseResult.getOrNull()

                    state.override {
                        if (lists.isNullOrEmpty()) {
                            EpisodeState.ErrorHoster(
                                throwable = firebaseResult.exceptionOrNull(),
                                episode = state.snapshot.episode
                            )
                        } else {
                            EpisodeState.SuccessHoster(
                                episode = state.snapshot.episode,
                                results = lists.toImmutableSet()
                            )
                        }
                    }
                }
            }
            inState<EpisodeState.SuccessHoster> {
                onEnter { state ->
                    val urls = state.snapshot.results
                    val streams = coroutineScope {
                        urls.map {
                            async {
                                Skeo.loadVideos(client, it)
                            }
                        }.awaitAll()
                    }.flatten()

                    state.override {
                        if (streams.isEmpty()) {
                            EpisodeState.ErrorStream(
                                throwable = null,
                                episode = state.snapshot.episode
                            )
                        } else {
                            EpisodeState.SuccessStream(
                                episode = state.snapshot.episode,
                                results = streams.toImmutableSet()
                            )
                        }
                    }
                }
            }
            inState<EpisodeState.ErrorHoster> {
                onEnterEffect {
                    crashlytics?.log(it.throwable)
                }
            }
        }
    }
}