package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.model.state.SaveAction
import dev.datlag.burningseries.model.state.SaveState
import dev.datlag.burningseries.network.Firestore
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.firebase.FireStore
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.ktor.client.*
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class SaveStateMachine(
    private val client: HttpClient,
    private val jsonBase: JsonBase,
    private val app: App?,
    private val firestore: FirebaseFirestore?,
    private val firestoreApi: Firestore?
) : FlowReduxStateMachine<SaveState, SaveAction>(initialState = SaveState.Waiting) {

    init {
        spec {
            inState<SaveState.Waiting> {
                onEnterEffect {
                    if (StateSaver.mongoUser == null) {
                        StateSaver.mongoUser = suspendCatching {
                            app?.login(Credentials.anonymous())
                        }.getOrNull()
                    }
                    if (StateSaver.firebaseUser == null) {
                        StateSaver.firebaseUser = suspendCatching {
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
                    val worked = FireStore.addStream(
                        firebaseUser = StateSaver.firebaseUser,
                        firestore = firestore,
                        firestoreApi = firestoreApi,
                        data = state.snapshot.data.firestore
                    )
                    println("Worked: $worked")
                    state.noChange()
                }
            }
        }
    }
}