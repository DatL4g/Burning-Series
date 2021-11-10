package de.datlag.model.burningseries.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class HomeData(
	val latestEpisodes: List<LatestEpisode> = listOf(),
	val latestSeries: List<LatestSeries> = listOf()
) : Parcelable