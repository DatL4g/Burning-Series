package dev.datlag.burningseries.github.model

import dev.datlag.burningseries.github.UserAndReleaseQuery
import dev.datlag.burningseries.model.common.toInt
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.fromFilePath
import io.ktor.http.fullPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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

    constructor(node: UserAndReleaseQuery.Node) : this(
        name = node.name,
        contentType = node.contentType.ifBlank { null },
        _downloadUrl = (node.downloadUrl as? CharSequence)?.toString()?.ifBlank { null } ?: node.downloadUrl.toString()
    )
}
