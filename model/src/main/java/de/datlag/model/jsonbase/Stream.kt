package de.datlag.model.jsonbase

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
@Obfuscate
data class Stream(
    @Transient var hoster: String = String(),
    @SerialName("url") val url: String = String(),
    @SerialName("embed") val embed: Boolean = false
) : Parcelable
