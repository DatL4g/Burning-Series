package de.datlag.model.video

import android.os.Parcelable
import de.datlag.model.burningseries.stream.StreamConfig
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize

@Parcelize
@Obfuscate
data class VideoStream(
    val hoster: String,
    val defaultUrl: String,
    val url: List<String>,
    val config: StreamConfig
) : Parcelable
