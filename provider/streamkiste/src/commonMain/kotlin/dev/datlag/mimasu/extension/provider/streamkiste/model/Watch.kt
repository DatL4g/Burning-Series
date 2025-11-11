package dev.datlag.mimasu.extension.provider.streamkiste.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Watch(
    @SerialName("s") val season: Int? = null,
    @SerialName("lang") val lang: Int? = null,
    @SerialName("streams") private val _streams: Set<Stream> = emptySet()
) {

    @Transient
    val streams = _streams.filterNot { it.isDeleted() }

    @Serializable
    data class Stream(
        @SerialName("stream") private val _stream: String,
        @SerialName("e") private val _episode: String? = null,
        @SerialName("deleted") val deleted: Int = 0,
        @SerialName("deleted_on") val deletedOn: String? = null
    ) {

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
            return deleted == 1 || !deletedOn.isNullOrBlank()
        }
    }
}
