package dev.datlag.mimasu.extension.provider.streamkiste.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Season(
    @SerialName("_id") val id: String,
    @SerialName("s") private val _season: String? = null
) {

    @Transient
    val season = _season?.toIntOrNull()
}
