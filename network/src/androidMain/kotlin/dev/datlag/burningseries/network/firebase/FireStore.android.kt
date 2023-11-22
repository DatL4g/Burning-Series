package dev.datlag.burningseries.network.firebase

import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.Firestore
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where

actual object FireStore {
    actual suspend fun getStreams(
        firestore: FirebaseFirestore,
        firestoreApi: Firestore,
        idList: List<String>
    ): List<String> {
        return (suspendCatching {
            firestore.collection("stream").where("id", inArray = idList).get().documents.map {
                it.get<String>("url")
            }
        }.getOrNull() ?: emptyList()).ifEmpty {
            RESTFireStore.getStreams(firestore, firestoreApi, idList)
        }
    }

    actual suspend fun addStream(
        firebaseUser: FirebaseUser?,
        firestore: FirebaseFirestore?,
        firestoreApi: Firestore?,
        data: HosterScraping.Firestore
    ): Boolean {
        if (firestore == null) {
            return RESTFireStore.addStream(firebaseUser, firestore, firestoreApi, data)
        }

        return suspendCatching {
            val existing = firestore
                .collection("stream")
                .where("id", equalTo = data.id)
                .get()
                .documents
                .firstOrNull()
                ?.reference
                ?: firestore.collection("stream").document

            firestore.runTransaction {
                set(documentRef = existing, data = data, merge = true)
            }
        }.isSuccess || RESTFireStore.addStream(firebaseUser, firestore, firestoreApi, data)
    }
}