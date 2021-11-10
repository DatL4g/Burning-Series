package de.datlag.model.burningseries.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LatestSeries(
	val title: String = String(),
	val href: String = String()
) : Parcelable
