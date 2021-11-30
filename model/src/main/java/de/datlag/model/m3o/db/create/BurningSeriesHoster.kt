package de.datlag.model.m3o.db.create


import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class BurningSeriesHoster(
    @SerialName("table") val table: String = "BurningSeries",
    @SerialName("record") val record: BurningSeriesHosterRecord
) : Parcelable