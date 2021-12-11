package de.datlag.model.burningseries.series

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.room.*
import de.datlag.model.Constants
import de.datlag.model.burningseries.common.encodeToHref
import de.datlag.model.burningseries.common.getDigitsOrNull
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Clock
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
	tableName = "SeriesTable",
	indices = [
		Index("seriesId"),
		Index("hrefTitle", unique = true)
	]
)
@Obfuscate
data class SeriesData(
	@ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
	@ColumnInfo(name = "season") @SerialName("season") var season: String = String(),
	@ColumnInfo(name = "description") @SerialName("description") val description: String = String(),
	@ColumnInfo(name = "image") @SerialName("image") val image: String = String(),
	@ColumnInfo(name = "hrefTitle") val hrefTitle: String = title.encodeToHref(),
	@ColumnInfo(name = "href") var href: String = "serie/$hrefTitle",
	@ColumnInfo(name = "updatedAt") var updatedAt: Long = Clock.System.now().epochSeconds,
	@ColumnInfo(name = "favoriteSince") var favoriteSince: Long = 0L,
	@ColumnInfo(name = "selectedLanguage") @SerialName("selectedLanguage") var selectedLanguage: String = String(),
	@Ignore @SerialName("infos") val infos: List<InfoData>,
	@Ignore @SerialName("languages") val languages: List<LanguageData>,
	@Ignore @SerialName("seasons") val seasons: List<String>,
	@Ignore @SerialName("episodes") val episodes: List<EpisodeInfo>
) : Parcelable {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "seriesId")
	@IgnoredOnParcel
	var seriesId: Long = 0L

	constructor(
		title: String = String(),
		season: String = String(),
		description: String = String(),
		image: String = String(),
		hrefTitle: String = title.encodeToHref(),
		href: String = "serie/$hrefTitle",
		updatedAt: Long = Clock.System.now().epochSeconds,
		favoriteSince: Long = 0L,
		selectedLanguage: String = String()
	) : this(
		title,
		season,
		description,
		image,
		hrefTitle,
		href,
		updatedAt,
		favoriteSince,
		selectedLanguage,
		listOf(),
		listOf(),
		listOf(),
		listOf()
	)

	fun currentSeason(seasons: List<SeasonData>): String {
		val foundSeason = seasons.find {
			it.title.equals(season, true)
					|| it.title.trim().equals(season.trim(), true)
					|| it.title.equals(season.toIntOrNull()?.toString(), true)
					|| it.title.toIntOrNull()?.toString()?.equals(season, true) == true
					|| it.title.toIntOrNull()?.toString()?.equals(season.toIntOrNull()?.toString(), true) == true
					|| it.title.getDigitsOrNull()?.equals(season, true) == true
					|| it.title.getDigitsOrNull()?.equals(season.getDigitsOrNull(), true) == true
		} ?: seasons.first()
		return foundSeason.title
	}

	fun hrefBuilder(season: String, language: String = selectedLanguage): String {
		var verifiedHref = href
		if (verifiedHref.endsWith('/')) {
			verifiedHref = verifiedHref.substring(0..(verifiedHref.length - 2))
		}
		val splitHref = verifiedHref.split('/')
		var joinedHref = splitHref.joinToString(separator = "/", limit = 2, truncated = String())
		if (!joinedHref.endsWith('/')) {
			joinedHref += '/'
		}
		return "$joinedHref$season/$language"
	}
}