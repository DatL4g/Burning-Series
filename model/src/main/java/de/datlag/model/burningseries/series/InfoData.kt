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
    tableName = "InfoTable",
    indices = [
        Index("infoId"),
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
data class InfoData(
    @ColumnInfo(name = "header") @SerialName("header") val header: String = String(),
    @ColumnInfo(name = "data") @SerialName("data") val data: String = String(),
    @ColumnInfo(name = "seriesId") var seriesId: Long = 0L
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "infoId")
    @IgnoredOnParcel
    var infoId: Long = 0L
}
