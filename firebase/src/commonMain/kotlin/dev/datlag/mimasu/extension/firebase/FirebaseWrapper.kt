package dev.datlag.mimasu.extension.firebase

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.model.ScrapedData
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.tooling.async.scopeCatching
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.listFrom
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.hours

class FirebaseWrapper(
    private val app: FirebaseApp = Firebase.app
) {

    val auth = Auth()
    val store = Store()

    inner class Auth internal constructor() {

        val isSignedIn: Boolean
            get() = scopeCatching {
                Firebase.auth(app).currentUser
            }.getOrNull() != null

        val signedIn = Firebase.auth(app).authStateChanged.map { u ->
            u != null
        }

        suspend fun signInAnonymously(): Boolean {
            if (isSignedIn) {
                return true
            }

            return suspendCatching {
                Firebase.auth(app).signInAnonymously().user
            }.getOrNull() != null
        }

        suspend fun signOut(): Boolean {
            val deleted = suspendCatching {
                Firebase.auth(app).currentUser?.delete()
            }.isSuccess
            val signedOut = suspendCatching {
                Firebase.auth(app).signOut()
            }.isSuccess

            return deleted && signedOut
        }
    }

    inner class Store internal constructor() {

        private val streamKache = InMemoryKache<String, String>(
            maxSize = 5L * 1024 * 1024
        ) {
            strategy = KacheStrategy.LRU
            expireAfterWriteDuration = 12.hours
        }

        suspend fun addStream(data: ScrapedData): Boolean {
            val fireStore = Firebase.firestore(app)
            val document = fireStore.collection("stream").where {
                "id" equalTo data.fireStore.id
            }.get().documents.firstOrNull()?.reference ?: fireStore.collection("stream").document

            fireStore.runTransaction {
                set(document, data = data.fireStore, merge = true)
            }
            return true
        }

        suspend fun streams(hrefList: Collection<String>): List<String> {
            val all = hrefList.map {
                it to streamKache.async(it)?.ifBlank { null }
            }
            val nonCached = all.filter {
                it.second.isNullOrBlank()
            }.map { it.first }

            if (nonCached.isEmpty()) {
                return all.mapNotNull { it.second?.ifBlank { null } }.distinct()
            }

            val query = Firebase.firestore(app).collection("stream").where {
                all(
                    *listOfNotNull(
                        nonCached.let { "id" inArray it }
                    ).toTypedArray()
                )
            }
            val loaded = query.get().documents.ifEmpty {
                return all.mapNotNull { it.second?.ifBlank { null } }.distinct()
            }.map { doc ->
                val id = doc.get<String>("id")
                val url = doc.get<String>("url")

                streamKache.async(id) { url } ?: url
            }

            return listFrom(
                all.mapNotNull { it.second?.ifBlank { null } },
                loaded
            ).distinct()
        }
    }

    sealed interface Creator {
        data class Available(val wrapper: FirebaseWrapper) : Creator
        data object Empty : Creator
    }
}