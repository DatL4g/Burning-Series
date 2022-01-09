package de.datlag.model.burningseries.series

import android.os.Parcelable
import androidx.room.*
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "EpisodeInfoTable",
    indices = [
        Index("episodeId"),
        Index("seriesId"),
        Index("href", unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = SeriesData::class,
            parentColumns = ["seriesId"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
@Obfuscate
data class EpisodeInfo(
    @ColumnInfo(name = "number") @SerialName("number") val number: String = String(),
    @ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
    @ColumnInfo(name = "href") @SerialName("href") val href: String = String(),
    @ColumnInfo(name = "seriesId") var seriesId: Long = 0L,
    @ColumnInfo(name = "currentWatchPos", defaultValue = 0.toString()) var currentWatchPos: Long = 0L,
    @ColumnInfo(name = "totalWatchPos", defaultValue = 0.toString()) var totalWatchPos: Long = 0L,
    @Ignore @SerialName("hoster") val hoster: List<HosterData>
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "episodeId")
    @IgnoredOnParcel
    var episodeId: Long = 0L

    constructor(
        number: String,
        title: String,
        href: String,
        seriesId: Long,
        currentWatchPos: Long,
        totalWatchPos: Long
    ) : this(number, title, href, seriesId, currentWatchPos, totalWatchPos, listOf())

    fun watchedPercentage(): Float {
        if (currentWatchPos == 0L || totalWatchPos == 0L) {
            return 0F
        } else if (totalWatchPos in 1 until currentWatchPos) {
            return 100F
        }
        return ((currentWatchPos.toDouble() * 100) / totalWatchPos.toDouble()).toFloat()
    }
}
