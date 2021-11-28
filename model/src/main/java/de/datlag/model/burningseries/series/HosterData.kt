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
    tableName = "HosterTable",
    indices = [
        Index("hosterId"),
        Index("episodeId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = EpisodeInfo::class,
            parentColumns = ["episodeId"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
@Obfuscate
data class HosterData(
    @ColumnInfo(name = "title") @SerialName("title") val title: String = String(),
    @ColumnInfo(name = "href") @SerialName("href") val href: String = String(),
    @ColumnInfo(name = "episodeId") var episodeId: Long = 0L
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "hosterId")
    @IgnoredOnParcel
    var hosterId: Long = 0L
}
