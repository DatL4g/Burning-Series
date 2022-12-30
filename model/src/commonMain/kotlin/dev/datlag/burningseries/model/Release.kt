package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.common.asIsoString
import dev.datlag.burningseries.model.common.getDigitsOrNull
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
data class Release(
    @SerialName("url") val url: String,
    @SerialName("assets_url") val assetsUrl: String = String(),
    @SerialName("html_url") val htmlUrl: String = String(),
    @SerialName("tag_name") val tagName: String = String(),
    @SerialName("name") val title: String = String(),
    @SerialName("draft") val isDraft: Boolean = false,
    @SerialName("prerelease") val isPreRelease: Boolean = false,
    @SerialName("published_at") val publishedAt: String = String(),
    @Transient val publishedAtSeconds: Long = try { publishedAt.toInstant().epochSeconds } catch (ignored: Exception) { 0L }
) : Parcelable {

    fun tagAsNumberString() = tagName.getDigitsOrNull()

    fun publishedAtIsoDate() = if (publishedAtSeconds > 0L) {
        val date = Instant.fromEpochSeconds(publishedAtSeconds).toLocalDateTime(TimeZone.currentSystemDefault()).date
        date.asIsoString()
    } else {
        publishedAt
    }
}
