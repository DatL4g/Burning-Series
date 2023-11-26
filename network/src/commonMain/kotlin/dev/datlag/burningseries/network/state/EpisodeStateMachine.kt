package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.algorithm.MD5
import dev.datlag.burningseries.model.common.setFrom
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.model.state.EpisodeAction
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.network.Firestore
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.firebase.FireStore
import dev.datlag.burningseries.network.realm.RealmLoader
import dev.datlag.burningseries.network.scraper.Video
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeStateMachine(
    private val client: HttpClient,
    private val jsonBase: JsonBase,
    private val realmLoader: RealmLoader,
    private val firestore: FirebaseFirestore?,
    private val firestoreApi: Firestore?
) : FlowReduxStateMachine<EpisodeState, EpisodeAction>(initialState = EpisodeState.Waiting) {

    init {
        spec {
            inState<EpisodeState.Waiting> {
                onEnterEffect {
                    realmLoader.login()
                    if (NetworkStateSaver.firebaseUser == null) {
                        NetworkStateSaver.firebaseUser = suspendCatching {
                            Firebase.auth.signInAnonymously().user
                        }.getOrNull()
                    }
                }
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.Loading> {
                onEnter { state ->
                    val episodeHref = state.snapshot.episode.href
                    val hosterHref = state.snapshot.episode.hosters.map { it.href }

                    val allResults = coroutineScope {
                        val jsonBaseResults = async {
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

                        val mongoHoster = NetworkStateSaver.mongoHosterMap[episodeHref] ?: emptyList()
                        val mongoDBResults = async {
                            mongoHoster.ifEmpty {
                                val newList = realmLoader.loadEpisodes(hosterHref)
                                NetworkStateSaver.mongoHosterMap[episodeHref] = newList
                                newList
                            }
                        }

                        val firebaseResults = async {
                            suspendCatching {
                                if (firestore != null && firestoreApi != null) {
                                    FireStore.getStreams(firestore, firestoreApi, hosterHref)
                                } else {
                                    null
                                }
                            }.getOrNull() ?: emptyList()
                        }

                        return@coroutineScope setFrom(
                            jsonBaseResults.await(),
                            mongoDBResults.await(),
                            firebaseResults.await()
                        )
                    }

                    if (allResults.isNotEmpty()) {
                        state.override { EpisodeState.SuccessHoster(
                            episode = state.snapshot.episode,
                            results = allResults
                        ) }
                    } else {
                        state.override { EpisodeState.ErrorHoster(state.snapshot.episode) }
                    }
                }
            }

            inState<EpisodeState.SuccessHoster> {
                onEnter { state ->
                    val urls = state.snapshot.results
                    val streams = coroutineScope {
                        urls.map { async {
                            Video.loadVideos(client, it)
                        } }.awaitAll()
                    }.filterNotNull()

                    if (streams.isEmpty()) {
                        state.override { EpisodeState.ErrorStream(state.snapshot.episode) }
                    } else {
                        state.override { EpisodeState.SuccessStream(state.snapshot.episode, streams) }
                    }
                }
            }

            inState<EpisodeState.SuccessStream> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.ErrorHoster> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.ErrorStream> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }
        }
    }
}