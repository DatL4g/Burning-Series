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
import dev.datlag.burningseries.network.scraper.Video
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import io.ktor.client.*
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.ext.call
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonPrimitive
import org.mongodb.kbson.BsonDocument

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeStateMachine(
    private val client: HttpClient,
    private val jsonBase: JsonBase,
    private val app: App?,
    private val firestore: FirebaseFirestore?,
    private val firestoreApi: Firestore?
) : FlowReduxStateMachine<EpisodeState, EpisodeAction>(initialState = EpisodeState.Waiting) {

    private var mongoUser: User? = null
    private val mongoHosterMap: MutableMap<String, List<String>> = mutableMapOf()

    private var firebaseUser: FirebaseUser? = null

    init {
        spec {
            inState<EpisodeState.Waiting> {
                onEnterEffect {
                    if (mongoUser == null) {
                        mongoUser = suspendCatching {
                            app?.login(Credentials.anonymous())
                        }.getOrNull()
                    }
                    if (firebaseUser == null) {
                        firebaseUser = suspendCatching {
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
                    val mongoHoster = mongoHosterMap[episodeHref] ?: emptyList()
                    val mongoDBResults = mongoHoster.ifEmpty {
                        val newList = suspendCatching {
                            val doc = mongoUser!!.functions.call<BsonDocument>("query", hosterHref.toTypedArray())
                            doc.getArray("result").values.map { it.asDocument().getString("url").value }
                        }.getOrNull() ?: emptyList()

                        mongoHosterMap[episodeHref] = newList
                        newList
                    }

                    val firebaseResults = suspendCatching {
                        if (firestore != null && firestoreApi != null) {
                            FireStore.getStreams(firestore, firestoreApi, hosterHref)
                        } else {
                            null
                        }
                    }.getOrNull() ?: emptyList()

                    if (jsonBaseResults.isNotEmpty() || mongoDBResults.isNotEmpty()) {
                        state.override { EpisodeState.SuccessHoster(
                            episode = state.snapshot.episode,
                            results = setFrom(
                                jsonBaseResults,
                                mongoDBResults,
                                firebaseResults
                            )
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
                        state.override { EpisodeState.SuccessStream(streams) }
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