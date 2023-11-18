package dev.datlag.burningseries.network.firebase

import dev.datlag.burningseries.network.Firestore
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where

actual object FireStore {
    actual suspend fun getStreams(
        firestore: FirebaseFirestore,
        firestoreApi: Firestore,
        idList: List<String>
    ): List<String> {
        return firestore.collection("stream").where("id", inArray = idList).get().documents.map {
            it.get<String>("url")
        }
    }
}