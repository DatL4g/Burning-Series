package dev.datlag.mimasu.extension.provider.streamkiste.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Watch(
    @SerialName("s") private val _season: String? = null,
    @SerialName("lang") private val _lang: String? = null,
    @SerialName("streams") private val _streams: Set<Stream> = emptySet()
) {

    @Transient
    val season = _season?.toIntOrNull()

    @Transient
    val lang = _lang?.toIntOrNull()

    @Transient
    val streams = _streams.filterNot { it.isDeleted() }

    @Serializable
    data class Stream(
        @SerialName("stream") private val _stream: String,
        @SerialName("e") private val _episode: String? = null,
        @SerialName("deleted") private val _deleted: String? = null,
        @SerialName("deleted_on") val deletedOn: String? = null
    ) {

        @Transient
        val deleted = _deleted?.toIntOrNull() == 1 || _deleted.toBoolean()

        @Transient
        val episode: Int? = _episode?.toIntOrNull()

        @Transient
        val stream = _stream.let {
            if (it.startsWith("//")) {
                "https:$it"
            } else {
                it
            }
        }

        fun isDeleted(): Boolean {
            return stream.isBlank() || deleted == 1 || !deletedOn.isNullOrBlank()
        }
    }
}
