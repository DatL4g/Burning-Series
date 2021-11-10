package de.datlag.model.burningseries.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LatestEpisode(
	val title: String = String(),
	val href: String = String()
) : Parcelable {
	fun getEpisodeAndSeries(): Pair<String, String> {
		val match = Regex(
			"^(.+(:|\\|))(.+)\$",
			setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
		).find(title)
		
		return Pair(
			match?.groupValues?.get(1)?.trim()?.dropLast(1) ?: String(),
			match?.groupValues?.get(3)?.trim() ?: String()
		)
	}
}
