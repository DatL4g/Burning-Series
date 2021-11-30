package de.datlag.model.m3o.db

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class Count(
    @SerialName("count") val count: Long = 0L
) : Parcelable {

    @Parcelize
    @Serializable
    @Obfuscate
    data class Request(
        @SerialName("table") val table: String
    ) : Parcelable
}
