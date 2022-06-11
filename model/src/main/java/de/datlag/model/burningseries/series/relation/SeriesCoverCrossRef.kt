package de.datlag.model.burningseries.series.relation

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
    tableName = "SeriesCoverCrossRef",
    primaryKeys = ["seriesId", "coverId"],
    indices = [
        Index("seriesId"),
        Index("coverId")
    ]
)
@Obfuscate
data class SeriesCoverCrossRef(
    @ColumnInfo(name = "seriesId") val seriesId: Long,
    @ColumnInfo(name = "coverId") val coverId: Long
) : Parcelable
