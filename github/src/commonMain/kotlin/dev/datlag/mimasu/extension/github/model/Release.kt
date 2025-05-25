package dev.datlag.mimasu.extension.github.model

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import dev.datlag.mimasu.extension.github.common.toInt

@Serializable
data class Release(
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val title: String,
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("prerelease") val preRelease: Boolean = false,
    @SerialName("assets") val assets: Set<Asset> = emptySet()
) {
    @Serializable
    data class Asset(
        @SerialName("name") val name: String,
        @SerialName("content_type") val contentType: String? = null,
        @SerialName("browser_download_url") private val _downloadUrl: String
    ) {

        @Transient
        val downloadUrl: Url = Url(_downloadUrl)

        @Transient
        val isApkContentType: Boolean = contentType.equals("application/vnd.android.package-archive", ignoreCase = true)

        @Transient
        val hasApkNameEnding: Boolean = name.endsWith(".apk")

        @Transient
        val hasApkUrlEnding: Boolean = downloadUrl.encodedPath.endsWith(".apk")

        @Transient
        val apkIdentifier: Int = isApkContentType.toInt() + hasApkNameEnding.toInt() + hasApkUrlEnding.toInt()

        @Transient
        val hasAnyApkIdentifier = apkIdentifier > 0
    }
}