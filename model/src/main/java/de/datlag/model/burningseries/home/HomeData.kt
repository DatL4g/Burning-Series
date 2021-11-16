package de.datlag.model.burningseries.home

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class HomeData(
	@SerialName("latestEpisodes") val latestEpisodes: List<LatestEpisode> = listOf(),
	@SerialName("latestSeries") val latestSeries: List<LatestSeries> = listOf()
) : Parcelable