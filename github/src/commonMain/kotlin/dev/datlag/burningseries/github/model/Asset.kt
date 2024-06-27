package dev.datlag.burningseries.github.model

import dev.datlag.burningseries.github.UserAndReleaseQuery
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    @SerialName("name") val name: String,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("browser_download_url") val downloadUrl: String
) {
    constructor(node: UserAndReleaseQuery.Node) : this(
        name = node.name,
        contentType = node.contentType.ifBlank { null },
        downloadUrl = (node.downloadUrl as? CharSequence)?.toString()?.ifBlank { null } ?: node.downloadUrl.toString()
    )
}
