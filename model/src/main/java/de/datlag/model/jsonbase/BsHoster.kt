package de.datlag.model.jsonbase

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class BsHoster(
    val url: String = String(),
    val embed: Boolean = false
) : Parcelable
