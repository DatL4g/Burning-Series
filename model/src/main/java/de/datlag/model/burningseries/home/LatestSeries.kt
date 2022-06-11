package de.datlag.model.burningseries.home

import android.os.Parcelable
import androidx.room.*
import de.datlag.model.burningseries.Cover
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
	tableName = "LatestSeriesTable",
	indices = [
		Index("latestSeriesId"),
		Index("href", unique = true)
	]
)
@Obfuscate
data class LatestSeries(
	@ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
	@ColumnInfo(name = "href") @SerialName("href") val href: String = String(),
	@ColumnInfo(name = "updatedAt") var updatedAt: Long = Clock.System.now().epochSeconds,
	@ColumnInfo(name = "nsfw", defaultValue = "false") @SerialName("isNsfw") val nsfw: Boolean = false,
	@Ignore @SerialName("cover") val cover: Cover
) : Parcelable {

	@PrimaryKey(autoGenerate = true)
	@IgnoredOnParcel
	@ColumnInfo(name = "latestSeriesId")
	var latestSeriesId: Long = 0L

	constructor(
		title: String = String(),
		href: String = String(),
		updatedAt: Long = Clock.System.now().epochSeconds,
		nsfw: Boolean = false
	) : this(title, href, updatedAt, nsfw, Cover())

	fun getHrefTitle(): String {
		val normHref = if (href.startsWith("/")) {
			href.substring(1)
		} else { href }
		val match = Regex("(/(\\w|-)+)").find(normHref)
		return match?.groupValues?.getOrNull(1)?.replace("/", "") ?: title.encodeToHref()
	}
}
