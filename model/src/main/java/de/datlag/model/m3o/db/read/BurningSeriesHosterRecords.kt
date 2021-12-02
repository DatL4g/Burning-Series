package de.datlag.model.m3o.db.read

import android.os.Parcelable
import de.datlag.model.m3o.db.create.BurningSeriesHosterRecord
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class BurningSeriesHosterRecords(
    @SerialName("records") val records: List<BurningSeriesHosterRecord> = listOf()
) : Parcelable
