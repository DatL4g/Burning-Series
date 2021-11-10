package de.datlag.model.burningseries.series

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Series(
	val success: Boolean = false,
	val data: SeriesData? = null
) : Parcelable
