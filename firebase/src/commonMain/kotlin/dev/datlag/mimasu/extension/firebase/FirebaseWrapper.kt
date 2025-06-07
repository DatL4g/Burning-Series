package dev.datlag.mimasu.extension.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.firestore.firestore

class FirebaseWrapper(
    private val app: FirebaseApp
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

}