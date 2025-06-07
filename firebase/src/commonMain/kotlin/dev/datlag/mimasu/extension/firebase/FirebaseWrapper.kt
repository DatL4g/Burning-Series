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
        suspend fun streams(hrefList: List<String>): List<String> {
            return Firebase.firestore(app).collection("stream").where {
                all(
                    *listOfNotNull(
                        hrefList.let { "id" inArray it }
                    ).toTypedArray()
                )
            }.get().documents.map {
                it.get<String>("url")
            }
        }
    }

    sealed interface Creator {
        data class Available(val wrapper: FirebaseWrapper) : Creator
        data object Empty : Creator
    }
}