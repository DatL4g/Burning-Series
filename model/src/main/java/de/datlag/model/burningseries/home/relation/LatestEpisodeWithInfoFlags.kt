package de.datlag.model.burningseries.home.relation

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestEpisodeInfoFlags
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class LatestEpisodeWithInfoFlags(
    @Embedded val latestEpisode: LatestEpisode,
    @Relation(
        parentColumn = "latestEpisodeId",
        entityColumn = "latestEpisodeInfoFlagsId",
        associateBy = Junction(LatestEpisodeInfoFlagsCrossRef::class)
    ) val infoFlags: List<LatestEpisodeInfoFlags>
) : Parcelable
