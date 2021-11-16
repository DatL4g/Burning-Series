package de.datlag.model.burningseries.series

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class Series(
	@SerialName("success") val success: Boolean = false,
	@SerialName("data") val data: SeriesData? = null
) : Parcelable
