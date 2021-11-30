package de.datlag.model.m3o.db.create

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BurningSeriesHosterRecord(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("embedded") val embedded: Boolean = false
) : Parcelable