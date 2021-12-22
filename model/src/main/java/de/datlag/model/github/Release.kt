package de.datlag.model.github

import android.os.Parcelable
import de.datlag.model.burningseries.common.getDigitsOrNull
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
@Obfuscate
data class Release(
    @SerialName("url") val url: String,
    @SerialName("assets_url") val assetsUrl: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("id") val id: String,
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val title: String,
    @SerialName("draft") val isDraft: Boolean,
    @SerialName("prerelease") val isPreRelease: Boolean,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("body") val body: String,
    @Transient val publishedAtSeconds: Long = try { publishedAt.toInstant().epochSeconds } catch (ignored: Exception) { 0L }
) : Parcelable {

    fun tagAsNumberString() = tagName.getDigitsOrNull()

    fun publishedAtIsoDate() = if (publishedAtSeconds > 0L) {
        val date = Instant.fromEpochSeconds(publishedAtSeconds).toLocalDateTime(TimeZone.currentSystemDefault()).date
        "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
    } else {
        publishedAt
    }
}
