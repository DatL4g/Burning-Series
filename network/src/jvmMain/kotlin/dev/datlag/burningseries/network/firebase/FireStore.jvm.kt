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

    actual suspend fun addStream(
        firebaseUser: FirebaseUser?,
        firestore: FirebaseFirestore?,
        firestoreApi: Firestore?,
        data: HosterScraping.Firestore
    ): Boolean {
        if (firestoreApi == null) {
            return false
        }
        val token = firebaseUser?.getIdToken(false)?.let { "Bearer $it" } ?: return false

        val existing = suspendCatching {
            firestoreApi.query(
                FirestoreQuery(
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
                                op = FirestoreQuery.StructuredQuery.Where.FieldFilter.OP.EQUAL,
                                value = FirestoreQuery.Value(
                                    stringValue = data.id
                                )
                            )
                        )
                    )
                )
            )
        }.getOrNull()?.firstOrNull()

        val response = if (existing != null) {
            val docName = existing.document.name!!.removeSuffix("/").substringAfterLast("/")

            firestoreApi.patch(
                token = token,
                collection = "stream/$docName",
                request = FirestoreDocument(
                    fields = existing.document.fields.toMutableMap().apply {
                        put("url", FirestoreQuery.Value(
                            stringValue = data.url
                        ))
                    }
                )
            )
        } else {
            firestoreApi.create(
                token = token,
                collection = "stream",
                request = FirestoreDocument(
                    fields = mapOf(
                        "id" to FirestoreQuery.Value(
                            stringValue = data.id
                        ),
                        "url" to FirestoreQuery.Value(
                            stringValue = data.url
                        )
                    )
                )
            )
        }

        return response.status.isSuccess()
    }
}