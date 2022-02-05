package de.datlag.model.burningseries.series.relation

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.SeriesData
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
class SeriesWithEpisode(
    @Embedded val series: SeriesData,
    @Relation(
        entity = EpisodeInfo::class,
        parentColumn = "seriesId",
        entityColumn = "seriesId"
    ) val episodes: List<EpisodeInfo>
) : Parcelable