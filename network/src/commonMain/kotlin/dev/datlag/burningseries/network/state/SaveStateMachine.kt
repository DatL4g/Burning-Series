package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.algorithm.MD5
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.model.state.SaveAction
import dev.datlag.burningseries.model.state.SaveState
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
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalCoroutinesApi::class)
class SaveStateMachine(
    private val client: HttpClient,
    private val jsonBase: JsonBase,
    private val realmLoader: RealmLoader,
    private val firestore: FirebaseFirestore?,
    private val firestoreApi: Firestore?
) : FlowReduxStateMachine<SaveState, SaveAction>(initialState = SaveState.Waiting) {

    init {
        spec {
            inState<SaveState.Waiting> {
                onEnterEffect {
                    realmLoader.login()
                    if (NetworkStateSaver.firebaseUser == null) {
                        NetworkStateSaver.firebaseUser = suspendCatching {
                            Firebase.auth.signInAnonymously().user
                        }.getOrNull()
                    }
                }

                on<SaveAction.Save> { action, state ->
                    state.override { SaveState.Saving(action.data) }
                }
            }

            inState<SaveState.Saving> {
                onEnter { state ->
                    val anySuccess = coroutineScope {
                        val jsonBaseSaved = async {
                            suspendCatching {
                                jsonBase.setBurningSeriesCaptcha(MD5.hexString(state.snapshot.data.href), state.snapshot.data.jsonBase)
                            }.getOrNull() != null
                        }

                        val mongoSaved = async {
                            realmLoader.saveEpisode(state.snapshot.data.href, state.snapshot.data.url)
                        }

                        val firebaseSaved = async {
                            suspendCatching {
                                FireStore.addStream(
                                    firebaseUser = NetworkStateSaver.firebaseUser,
                                    firestore = firestore,
                                    firestoreApi = firestoreApi,
                                    data = state.snapshot.data.firestore
                                )
                            }.getOrNull() ?: false
                        }

                        return@coroutineScope jsonBaseSaved.await() || mongoSaved.await() || firebaseSaved.await()
                    }

                    val stream = Video.loadVideos(client, state.snapshot.data.url)

                    if (anySuccess) {
                        state.override { SaveState.Success(stream) }
                    } else {
                        state.override { SaveState.Error }
                    }
                }
            }

            inState<SaveState.Success> {
                on<SaveAction.Save> { action, state ->
                    state.override { SaveState.Saving(action.data) }
                }
            }

            inState<SaveState.Error> {
                on<SaveAction.Save> { action, state ->
                    state.override { SaveState.Saving(action.data) }
                }
            }
        }
    }
}