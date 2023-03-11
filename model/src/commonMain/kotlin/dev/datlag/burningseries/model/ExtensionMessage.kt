package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtensionMessage(
    @SerialName("query") val query: QueryType,
    @SerialName("id") val id: String,
    @SerialName("url") val url: String? = null
) {

    enum class QueryType {
        GET,
        EXISTS,
        SET;
    }
}
