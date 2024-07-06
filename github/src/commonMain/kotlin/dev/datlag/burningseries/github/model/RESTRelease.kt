package dev.datlag.burningseries.github.model

import dev.datlag.burningseries.model.serializer.SerializableImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RESTRelease(
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val title: String,
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("prerelease") val preRelease: Boolean = false,
    @SerialName("assets") val assets: SerializableImmutableSet<Asset> = persistentSetOf()
)
