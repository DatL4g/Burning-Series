package de.datlag.model.burningseries.home.relation

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "LatestSeriesCoverCrossRef",
    primaryKeys = ["latestSeriesId", "coverId"],
    indices = [
        Index("latestSeriesId"),
        Index("coverId")
    ]
)
@Obfuscate
data class LatestSeriesCoverCrossRef(
    @ColumnInfo(name = "latestSeriesId") val latestSeriesId: Long,
    @ColumnInfo(name = "coverId") val coverId: Long
) : Parcelable
