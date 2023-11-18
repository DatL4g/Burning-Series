package dev.datlag.burningseries.network.firebase

import dev.datlag.burningseries.model.FirestoreQuery
import dev.datlag.burningseries.network.Firestore
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore

actual object FireStore {
    actual suspend fun getStreams(
        firestore: FirebaseFirestore,
        firestoreApi: Firestore,
        idList: List<String>
    ): List<String> {
        val result = firestoreApi.query(FirestoreQuery(
            structuredQuery = FirestoreQuery.StructuredQuery(
                from = listOf(
                    FirestoreQuery.StructuredQuery.From(
                        collectionId = "stream"
                    )
                ),
                where = FirestoreQuery.StructuredQuery.Where(
                    fieldFilter = FirestoreQuery.StructuredQuery.Where.FieldFilter(
                        field = FirestoreQuery.StructuredQuery.Field(
                            fieldPath = "id"
                        ),
                        op = FirestoreQuery.StructuredQuery.Where.FieldFilter.OP.IN,
                        value = FirestoreQuery.Value(
                            arrayValue = FirestoreQuery.Value.ArrayValue(
                                values = idList.map { FirestoreQuery.Value(
                                    stringValue = it
                                ) }
                            )
                        )
                    )
                ),
                select = FirestoreQuery.StructuredQuery.Select(
                    fields = listOf(
                        FirestoreQuery.StructuredQuery.Field(
                            fieldPath = "url"
                        )
                    )
                )
            )
        ))
        return result.mapNotNull {
            it.document.fields["url"]?.stringValue
        }
    }
}