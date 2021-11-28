package de.datlag.model.burningseries.series

import android.os.Parcelable
import androidx.room.*
import de.datlag.model.burningseries.common.encodeToHref
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
	@ColumnInfo(name = "updatedAt") var updatedAt: Long = Clock.System.now().epochSeconds,
	@ColumnInfo(name = "favoriteSince") var favoriteSince: Long = 0L,
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
		updatedAt: Long = Clock.System.now().epochSeconds,
		favoriteSince: Long = 0L
	) : this(
		title,
		season,
		description,
		image,
		hrefTitle,
		updatedAt,
		favoriteSince,
		listOf(),
		listOf(),
		listOf(),
		listOf()
	)
}