package dev.datlag.burningseries.network.firebase

import dev.datlag.burningseries.model.FirestoreDocument
import dev.datlag.burningseries.model.FirestoreQuery
import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.Firestore
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.ktor.client.statement.*
import io.ktor.http.*

actual object FireStore {
    actual suspend fun getStreams(
        firestore: FirebaseFirestore,
        firestoreApi: Firestore,
        idList: List<String>
    ): List<String> = RESTFireStore.getStreams(firestore, firestoreApi, idList)

    actual suspend fun addStream(
        firebaseUser: FirebaseUser?,
        firestore: FirebaseFirestore?,
        firestoreApi: Firestore?,
        data: HosterScraping.Firestore
    ): Boolean = RESTFireStore.addStream(firebaseUser, firestore, firestoreApi, data)
}