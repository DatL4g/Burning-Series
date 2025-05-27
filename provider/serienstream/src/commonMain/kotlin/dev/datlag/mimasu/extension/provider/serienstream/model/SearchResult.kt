package dev.datlag.mimasu.extension.provider.serienstream.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SearchResult(
    @SerialName("name") val name: String,
    @SerialName("link") val link: String,
    @SerialName("description") val description: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("productionYear") val productionYear: String? = null
) {

    @Transient
    val releaseYear = productionYear?.split("-")?.firstOrNull()?.replace(productionSanitizeRegex, "")?.trim()?.toIntOrNull()

    companion object {
        private val productionSanitizeRegex = "\\D".toRegex()
    }
}
