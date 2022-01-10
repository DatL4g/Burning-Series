package de.datlag.model.burningseries.series

import android.os.Parcelable
import androidx.room.*
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "SeasonTable",
    indices = [
        Index("seasonId"),
        Index("seriesId")
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
data class SeasonData(
    @ColumnInfo(name = "title") val title: String = String(),
    @ColumnInfo(name = "value") val value: Int = 1,
    @ColumnInfo(name = "seriesId") var seriesId: Long = 0L
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "seasonId")
    @IgnoredOnParcel
    var seasonId: Long = 0L
}
