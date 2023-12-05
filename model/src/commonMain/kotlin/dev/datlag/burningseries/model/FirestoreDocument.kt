package dev.datlag.burningseries.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class FirestoreDocument(
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("name") val name: String? = null,
    @SerialName("fields") val fields: Map<String, FirestoreQuery.Value>
)