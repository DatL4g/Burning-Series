package de.datlag.model.video

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize

@Parcelize
@Obfuscate
data class VideoStream(
    val hoster: String,
    val url: List<String>
) : Parcelable
