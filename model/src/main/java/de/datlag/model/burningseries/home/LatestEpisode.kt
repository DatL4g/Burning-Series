package de.datlag.model.burningseries.home

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
	tableName = "LatestEpisodeTable",
	indices = [
		Index("latestEpisodeId"),
		Index("href", unique = true)
	]
)
@Obfuscate
data class LatestEpisode(
	@ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
	@ColumnInfo(name = "href") @SerialName("href") val href: String = String(),
	@ColumnInfo(name = "updatedAt") var updatedAt: Long = Clock.System.now().epochSeconds
) : Parcelable {

	@PrimaryKey(autoGenerate = true)
	@IgnoredOnParcel
	@ColumnInfo(name = "latestEpisodeId")
	var latestEpisodeId: Long = 0L

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

	fun getHrefTitle(): String {
		val normHref = if (href.startsWith("/")) {
			href.substring(1)
		} else { href }
		val match = Regex("(/(\\w|-)+)").find(normHref)
		return match?.groupValues?.getOrNull(1)?.replace("/", "") ?: getEpisodeAndSeries().first.encodeToHref()
	}

	fun getHrefWithoutEpisode(): String {
		val hrefSplit = href.split('/')
		return hrefSplit.subList(0, 3).joinToString("/")
	}
}
