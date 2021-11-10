package de.datlag.model.burningseries.series

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.datlag.model.burningseries.common.encodeToHref
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
data class SeriesData(
	@ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
	@ColumnInfo(name = "season") @SerialName("season") val season: String = String(),
	@ColumnInfo(name = "description") @SerialName("description") val description: String = String(),
	@ColumnInfo(name = "image") @SerialName("image") val image: String = String(),
	@ColumnInfo(name = "hrefTitle") val hrefTitle: String = title.encodeToHref(),
	@ColumnInfo(name = "updatedAt") var updatedAt: Long = Clock.System.now().epochSeconds
) : Parcelable {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "seriesId")
	@IgnoredOnParcel
	var seriesId: Long = 0L
}