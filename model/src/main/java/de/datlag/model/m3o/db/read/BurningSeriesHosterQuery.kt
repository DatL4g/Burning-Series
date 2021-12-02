package de.datlag.model.m3o.db.read

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class BurningSeriesHosterQuery(
    @SerialName("id") val id: String,
    @SerialName("limit") val limit: Int = 1,
    @SerialName("table") val table: String = "BurningSeries"
) : Parcelable
