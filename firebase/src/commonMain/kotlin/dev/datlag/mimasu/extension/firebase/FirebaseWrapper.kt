package dev.datlag.mimasu.extension.firebase

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.tooling.listFrom
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.app
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Duration.Companion.hours

class FirebaseWrapper(
    private val app: FirebaseApp = Firebase.app
) {

    val store = Store()

    inner class Store internal constructor() {

        private val streamKache = InMemoryKache<String, String>(
            maxSize = 5L * 1024 * 1024
        ) {
            strategy = KacheStrategy.LRU
            expireAfterWriteDuration = 12.hours
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