package de.datlag.model.burningseries.allseries

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class AllSeries(
    @SerialName("success") val success: Boolean = false,
    @SerialName("data") val data: List<GenreModel.GenreData> = listOf()
) : Parcelable
