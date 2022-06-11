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
    tableName = "LatestEpisodeCoverCrossRef",
    primaryKeys = ["latestEpisodeId", "coverId"],
    indices = [
        Index("latestEpisodeId"),
        Index("coverId")
    ]
)
@Obfuscate
data class LatestEpisodeCoverCrossRef(
    @ColumnInfo(name = "latestEpisodeId") val latestEpisodeId: Long,
    @ColumnInfo(name = "coverId") val coverId: Long
): Parcelable
