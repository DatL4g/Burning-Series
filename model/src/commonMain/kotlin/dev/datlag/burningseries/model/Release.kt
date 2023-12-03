package dev.datlag.burningseries.model

import dev.datlag.burningseries.model.common.getDigitsOrNull
import dev.datlag.burningseries.model.common.scopeCatching
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Release(
    @SerialName("url") val url: String,
    @SerialName("html_url") val htmlUrl: String = url,
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val title: String,
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("prerelease") val preRelease: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null,
    @Transient val publishedAtSeconds: Long = scopeCatching { publishedAt?.toInstant()?.epochSeconds }.getOrNull() ?: 0L
) {

    @Transient
    val tagAsNumber: String? = tagName.getDigitsOrNull()
}
