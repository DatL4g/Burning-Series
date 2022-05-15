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
    tableName = "LatestEpisodeInfoFlagsCrossRef",
    primaryKeys = ["latestEpisodeId", "latestEpisodeInfoFlagsId"],
    indices = [
        Index("latestEpisodeId"),
        Index("latestEpisodeInfoFlagsId")
    ]
)
@Obfuscate
data class LatestEpisodeInfoFlagsCrossRef(
    @ColumnInfo(name = "latestEpisodeId") val latestEpisodeId: Long,
    @ColumnInfo(name = "latestEpisodeInfoFlagsId") val latestEpisodeInfoFlagsId: Long
): Parcelable
