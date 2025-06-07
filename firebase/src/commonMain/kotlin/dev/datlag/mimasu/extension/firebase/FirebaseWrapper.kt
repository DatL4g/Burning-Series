package dev.datlag.mimasu.extension.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.app
import dev.gitlive.firebase.firestore.firestore

class FirebaseWrapper(
    private val app: FirebaseApp = Firebase.app
) {

    val store = Store()

    inner class Store internal constructor() {

        private val streamCache = mutableMapOf<String, String>()

        suspend fun streams(hrefList: List<String>): List<String> {
            val all = hrefList.map {
                it to streamCache[it]?.ifBlank { null }
            }
            val nonCached = all.filter {
                it.second.isNullOrBlank()
            }.map { it.first }

            if (nonCached.isEmpty()) {
                return all.mapNotNull { it.second?.ifBlank { null } }.distinct()
            }

            val loaded = Firebase.firestore(app).collection("stream").where {
                all(
                    *listOfNotNull(
                        nonCached.let { "id" inArray it }
                    ).toTypedArray()
                )
            }.get().documents.map { doc ->
                val id = doc.get<String>("id")
                val url = doc.get<String>("url")

                streamCache[id] = url
                url
            }

            return (all.mapNotNull {
                it.second?.ifBlank { null }
            } + loaded).distinct()
        }
    }

    sealed interface Creator {
        data class Available(val wrapper: FirebaseWrapper) : Creator
        data object Empty : Creator
    }
}