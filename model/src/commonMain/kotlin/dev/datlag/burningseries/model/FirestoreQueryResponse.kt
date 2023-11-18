package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreQueryResponse(
    @SerialName("document") val document: Document
) {

    @Serializable
    data class Document(
        @SerialName("fields") val fields: Map<String, FirestoreQuery.Value>
    )
}