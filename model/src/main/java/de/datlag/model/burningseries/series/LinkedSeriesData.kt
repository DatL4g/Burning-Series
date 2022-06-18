package de.datlag.model.burningseries.series

import android.os.Parcelable
import androidx.room.*
import de.datlag.model.burningseries.HrefTitleBuilder
import de.datlag.model.burningseries.common.encodeToHref
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "LinkedSeriesTable",
    indices = [
        Index("linkedSeriesId"),
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
data class LinkedSeriesData(
    @ColumnInfo(name = "isSpinOff") @SerialName("isSpinOff") val isSpinOff: Boolean = false,
    @ColumnInfo(name = "isMainStory") @SerialName("isMainStory") val isMainStory: Boolean = false,
    @ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
    @ColumnInfo(name = "href") @SerialName("href") override val href: String = String(),
    @ColumnInfo(name = "seriesId") var seriesId: Long = 0L
) : Parcelable, HrefTitleBuilder() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "linkedSeriesId")
    @IgnoredOnParcel
    var linkedSeriesId: Long = 0L

    override fun hrefTitleFallback(): String {
        return title.encodeToHref()
    }
}
